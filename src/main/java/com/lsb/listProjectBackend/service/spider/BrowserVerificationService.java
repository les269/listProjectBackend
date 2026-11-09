package com.lsb.listProjectBackend.service.spider;

import com.lsb.listProjectBackend.config.BrowserVerificationProperties;
import com.lsb.listProjectBackend.domain.spider.BrowserVerificationTO.*;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import jakarta.annotation.PreDestroy;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

/** All Playwright calls and observer callbacks run on one worker thread. */
@Service
public class BrowserVerificationService {
    private final BrowserVerificationProperties properties;
    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "browser-verification");
        thread.setDaemon(true);
        return thread;
    });
    private Playwright playwright;
    private BrowserContext context;
    private Page page;
    private Session session;
    private ScheduledFuture<?> expiry;
    private ScheduledFuture<?> observer;
    private String readySelector;
    private String responseUrl;
    private String mitigated;
    private Integer browserHttpStatus;
    private int navigationCount;
    private int challengeResponseCount;
    private BrowserState state = BrowserState.LOADING;
    private String currentUrl = "";
    private String title = "";
    private boolean hasClearance;
    private Boolean selectorMatched;
    private String message = "Waiting for browser navigation";
    private Instant observedAt;
    private JsoupCheck jsoup = notTested();

    public BrowserVerificationService(BrowserVerificationProperties properties) {
        this.properties = properties;
    }

    public Session start(String url) {
        return start(url, null);
    }

    public Session start(String url, String selector) {
        if (!properties.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Browser verification is disabled");
        }
        validateUrl(url);
        if (selector != null && selector.length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "readySelector is too long");
        }
        if (selector != null && !selector.isBlank()) {
            try {
                Jsoup.parse("").select(selector);
            } catch (RuntimeException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "readySelector must be a valid CSS selector supported by Jsoup");
            }
        }
        return call(() -> {
            if (context != null && Instant.now().isBefore(session.expiresAt()) && !page.isClosed()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "A verification window is already open");
            }
            closeResources();
            session = null;
            resetObservation(selector);
            try {
                Path profile = profilePath(url);
                Files.createDirectories(profile);
                playwright = createPlaywright();
                var options = new BrowserType.LaunchPersistentContextOptions().setHeadless(false).setTimeout(30_000);
                if (properties.getChannel() != null && !properties.getChannel().isBlank()) {
                    options.setChannel(properties.getChannel());
                }
                context = playwright.chromium().launchPersistentContext(profile, options);
                page = context.pages().stream().filter(p -> !p.isClosed()).findFirst().orElseGet(context::newPage);
                page.onResponse(this::onResponse);
                session = new Session(UUID.randomUUID().toString(), url,
                        Instant.now().plusSeconds(Math.clamp(properties.getSessionTimeoutSeconds(), 30, 1800)));
                try {
                    page.navigate(url, new Page.NavigateOptions()
                            .setWaitUntil(WaitUntilState.DOMCONTENTLOADED).setTimeout(30_000));
                } catch (TimeoutError ignored) {
                    // Keep a slow/challenged page available for manual interaction and status inspection.
                }
                expiry = worker.schedule(() -> finish(BrowserState.EXPIRED, "Verification session expired"),
                        Math.clamp(properties.getSessionTimeoutSeconds(), 30, 1800), TimeUnit.SECONDS);
                observer = worker.scheduleWithFixedDelay(this::observe, 1, 1, TimeUnit.SECONDS);
                observe();
                return session;
            } catch (Exception e) {
                closeResources();
                session = null;
                // Raw browser errors may include sensitive URLs or profile contents.
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Cannot open verification browser. Check browser installation, profile directory and desktop availability.");
            }
        });
    }

    public Status status(String sessionId) {
        return call(() -> {
            requireKnownSession(sessionId);
            if (context != null) observe();
            return snapshot();
        });
    }

    /** Explicit network probe. Status polling never sends a Jsoup request or navigates Chrome. */
    public Status testJsoup(String sessionId) {
        return call(() -> {
            requireActiveSession(sessionId);
            observe();
            if (state == BrowserState.BROWSER_READY) probeJsoup();
            return snapshot();
        });
    }

    public VerifiedSession verify(String sessionId) {
        return call(() -> {
            requireActiveSession(sessionId);
            observe();
            if (state != BrowserState.BROWSER_READY) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Browser is not ready. Inspect the status endpoint before testing Jsoup or saving.");
            }
            VerifiedSession verified = probeJsoup();
            if (verified == null) {
                throw new ResponseStatusException(jsoup.httpStatus() == null ? HttpStatus.BAD_GATEWAY : HttpStatus.CONFLICT,
                        jsoup.message());
            }
            return verified;
        });
    }

    private VerifiedSession probeJsoup() {
        Map<String, String> cookies = readCookies();
        String userAgent = (String) page.evaluate("navigator.userAgent");
        try {
            Connection.Response response = request(session.url(), userAgent, cookies);
            String responseTitle = Jsoup.parse(response.body()).title();
            if (!isUsableResponse(response.statusCode(), response.header("cf-mitigated"), responseTitle)) {
                jsoup = new JsoupCheck(JsoupState.FAILED, response.statusCode(), responseTitle,
                        "Browser is ready but Jsoup receives a challenge, block or redirect. Nothing was saved.", Instant.now());
                return null;
            }
            if (readySelector != null && Jsoup.parse(response.body()).select(readySelector).isEmpty()) {
                jsoup = new JsoupCheck(JsoupState.FAILED, response.statusCode(), responseTitle,
                        "Jsoup content does not contain readySelector. Nothing was saved.", Instant.now());
                return null;
            }
            jsoup = new JsoupCheck(JsoupState.PASSED, response.statusCode(), responseTitle,
                    "Jsoup can reuse this session. Database has not been updated by this test.", Instant.now());
            return new VerifiedSession(cookies, userAgent, response.statusCode(), responseTitle);
        } catch (java.io.IOException e) {
            jsoup = new JsoupCheck(JsoupState.FAILED, null, null,
                    "Jsoup verification request failed. Nothing was saved.", Instant.now());
            return null;
        }
    }

    private void onResponse(Response response) {
        var request = response.request();
        if (!request.isNavigationRequest() || !request.frame().equals(page.mainFrame())) return;
        responseUrl = response.url();
        browserHttpStatus = response.status();
        mitigated = response.headerValue("cf-mitigated");
        navigationCount++;
        if ("challenge".equalsIgnoreCase(mitigated)) challengeResponseCount++;
        jsoup = notTested();
    }

    /** Read the browser only: never reload, click challenges, or perform extra site requests here. */
    private void observe() {
        if (context == null) return;
        if (!Instant.now().isBefore(session.expiresAt())) {
            finish(BrowserState.EXPIRED, "Verification session expired");
            return;
        }
        try {
            if (page.isClosed()) {
                finish(BrowserState.WINDOW_CLOSED, "Verification window was closed");
                return;
            }
            // Playwright Java dispatches browser events only while its message loop runs.
            page.waitForTimeout(1);
            String url = page.url();
            currentUrl = safeUrl(url);
            title = page.title();
            String clearance = readCookies().get("cf_clearance");
            hasClearance = clearance != null && !clearance.isBlank();
            selectorMatched = null;
            if (!sameOrigin(session.url(), url)) {
                setState(BrowserState.WRONG_ORIGIN, "Current page is not on the target origin");
            } else if (responseUrl == null || !withoutFragment(url).equals(withoutFragment(responseUrl))) {
                setState(BrowserState.LOADING, "Waiting for the current page navigation response");
            } else if ("challenge".equalsIgnoreCase(mitigated) || title.toLowerCase(Locale.ROOT).contains("just a moment")) {
                setState(BrowserState.CHALLENGED, "Browser is still showing a verification page");
            } else if (browserHttpStatus != null && browserHttpStatus >= 400) {
                setState(BrowserState.BLOCKED, "Browser received an HTTP error or block page");
            } else if (title.toLowerCase(Locale.ROOT).contains("access denied")
                    || title.toLowerCase(Locale.ROOT).contains("attention required")) {
                setState(BrowserState.BLOCKED, "Browser is showing an access denied page");
            } else if (browserHttpStatus == null || browserHttpStatus < 200 || browserHttpStatus >= 300
                    || title.isBlank() || "loading".equals(page.evaluate("document.readyState"))) {
                setState(BrowserState.LOADING, "Waiting for normal page content");
            } else {
                if (readySelector != null) selectorMatched = page.locator(readySelector).count() > 0;
                if (!hasClearance || Boolean.FALSE.equals(selectorMatched)) {
                    setState(BrowserState.WAITING_FOR_VERIFICATION,
                            "Waiting for clearance cookie and optional normal-page selector");
                } else {
                    setState(BrowserState.BROWSER_READY,
                            "Browser shows normal page content with clearance. Jsoup must be tested separately.");
                }
            }
        } catch (RuntimeException e) {
            if (page != null && page.isClosed()) finish(BrowserState.WINDOW_CLOSED, "Verification window was closed");
            else setState(BrowserState.ERROR, "Cannot inspect browser page. Check window state and readySelector.");
        }
    }

    private Map<String, String> readCookies() {
        Map<String, String> cookies = new LinkedHashMap<>();
        for (var cookie : context.cookies(session.url())) cookies.put(cookie.name, cookie.value);
        return cookies;
    }

    public void cancel(String sessionId) {
        call(() -> {
            requireKnownSession(sessionId);
            if (context != null) finish(BrowserState.CANCELLED, "Verification cancelled; profile retained");
            return null;
        });
    }

    public void release(String sessionId) {
        call(() -> {
            if (session != null && session.sessionId().equals(sessionId)) {
                finish(BrowserState.SAVED, "Session saved to database; browser closed and profile retained");
            }
            return null;
        });
    }

    protected Playwright createPlaywright() {
        return Playwright.create(new Playwright.CreateOptions()
                .setEnv(Map.of("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1")));
    }

    protected Connection.Response request(String url, String userAgent, Map<String, String> cookies)
            throws java.io.IOException {
        return Jsoup.connect(url).userAgent(userAgent).cookies(cookies)
                .header("Accept-Language", "zh-TW,zh;q=0.9,en-US;q=0.8,en;q=0.7,zh-CN;q=0.6,ja;q=0.5")
                .header("Accept", "*/*").header("Content-Type", "text/html; charset=UTF-8")
                .followRedirects(false).ignoreHttpErrors(true).ignoreContentType(true)
                .timeout(20_000).maxBodySize(2 * 1024 * 1024).execute();
    }

    Path profilePath(String url) {
        try {
            String key = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(origin(url).getBytes(StandardCharsets.UTF_8)));
            return Path.of(properties.getProfileDirectory()).toAbsolutePath().normalize().resolve(key);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private void requireKnownSession(String id) {
        if (session == null || !session.sessionId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Verification session not found");
        }
    }

    private void requireActiveSession(String id) {
        requireKnownSession(id);
        if (context != null) observe();
        if (context == null) {
            throw new ResponseStatusException(HttpStatus.GONE, "Verification session expired or window closed");
        }
    }

    void validateUrl(String url) {
        try {
            URI uri = URI.create(url);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || uri.getRawUserInfo() != null || (uri.getPort() != -1 && uri.getPort() != 443)
                    || properties.getAllowedHosts().stream().noneMatch(host -> host.equalsIgnoreCase(uri.getHost()))) {
                throw new IllegalArgumentException();
            }
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL must use HTTPS and an allowed host");
        }
    }

    private static String origin(String url) {
        URI uri = URI.create(url);
        int port = uri.getPort() == -1 ? 443 : uri.getPort();
        return uri.getScheme().toLowerCase(Locale.ROOT) + "://" + uri.getHost().toLowerCase(Locale.ROOT) + ":" + port;
    }

    private static boolean sameOrigin(String first, String second) {
        try { return origin(first).equals(origin(second)); }
        catch (RuntimeException e) { return false; }
    }

    private static String safeUrl(String url) {
        try {
            URI uri = URI.create(url);
            return new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), uri.getPath(), null, null).toString();
        } catch (Exception e) { return ""; }
    }

    private static String withoutFragment(String url) { return url.split("#", 2)[0]; }

    private static boolean challengeTitle(String title) {
        String lower = title.toLowerCase(Locale.ROOT);
        return lower.contains("just a moment") || lower.contains("attention required");
    }

    static boolean isUsableResponse(int status, String mitigated, String title) {
        return status >= 200 && status < 300 && !"challenge".equalsIgnoreCase(mitigated)
                && !challengeTitle(title) && !title.toLowerCase(Locale.ROOT).contains("access denied");
    }

    private void resetObservation(String selector) {
        readySelector = selector == null || selector.isBlank() ? null : selector;
        responseUrl = null;
        mitigated = null;
        browserHttpStatus = null;
        navigationCount = 0;
        challengeResponseCount = 0;
        currentUrl = "";
        title = "";
        hasClearance = false;
        selectorMatched = null;
        jsoup = notTested();
        setState(BrowserState.LOADING, "Waiting for browser navigation");
    }

    private static JsoupCheck notTested() {
        return new JsoupCheck(JsoupState.NOT_TESTED, null, null, "Jsoup has not been tested for this navigation", null);
    }

    private void setState(BrowserState value, String reason) {
        state = value;
        message = reason;
        observedAt = Instant.now();
    }

    private Status snapshot() {
        return new Status(session.sessionId(), state, currentUrl, title, browserHttpStatus, hasClearance,
                selectorMatched, navigationCount, challengeResponseCount, message, observedAt, session.expiresAt(), jsoup);
    }

    private <T> T call(Callable<T> operation) {
        try {
            return worker.submit(operation).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Verification request interrupted");
        } catch (ExecutionException e) {
            if (e.getCause() instanceof ResponseStatusException error) throw error;
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Browser verification operation failed");
        }
    }

    private void finish(BrowserState terminalState, String reason) {
        closeResources();
        setState(terminalState, reason);
    }

    private void closeResources() {
        if (expiry != null) { expiry.cancel(false); expiry = null; }
        if (observer != null) { observer.cancel(false); observer = null; }
        try {
            if (context != null) context.close();
        } catch (RuntimeException ignored) {
            // The user may already have closed the window.
        } finally {
            try {
                if (playwright != null) playwright.close();
            } catch (RuntimeException ignored) {
                // Cleanup must not turn a successful database save into an API failure.
            } finally {
                playwright = null;
                context = null;
                page = null;
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        worker.execute(this::closeResources);
        worker.shutdown();
        try {
            worker.awaitTermination(35, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Internal only: never serialize or log this object. */
    public static final class VerifiedSession {
        private final Map<String, String> cookies;
        private final String userAgent;
        private final int status;
        private final String title;

        public VerifiedSession(Map<String, String> cookies, String userAgent, int status, String title) {
            this.cookies = Map.copyOf(cookies);
            this.userAgent = userAgent;
            this.status = status;
            this.title = title;
        }

        public Map<String, String> cookies() { return cookies; }
        public String userAgent() { return userAgent; }
        public int status() { return status; }
        public String title() { return title; }
    }
}
