package com.lsb.listProjectBackend.service.spider;

import com.lsb.listProjectBackend.config.BrowserVerificationProperties;
import com.lsb.listProjectBackend.domain.spider.BrowserVerificationTO.*;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;
import org.jsoup.Connection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BrowserVerificationServiceTest {
    @TempDir
    Path profiles;
    private final BrowserVerificationProperties properties = new BrowserVerificationProperties();
    private final Playwright playwright = mock(Playwright.class);
    private final BrowserType chromium = mock(BrowserType.class);
    private final BrowserContext context = mock(BrowserContext.class);
    private final Page page = mock(Page.class);
    private final Frame mainFrame = mock(Frame.class);
    private final Response navigation = mock(Response.class);
    private final Request navigationRequest = mock(Request.class);
    private final Connection.Response response = mock(Connection.Response.class);
    private final AtomicReference<Thread> owner = new AtomicReference<>();
    private Consumer<Response> responseListener;
    private Map<String, String> exported;
    private int requestCount;
    private boolean networkFailure;
    private final BrowserVerificationService service = new BrowserVerificationService(properties) {
        @Override
        protected Playwright createPlaywright() {
            owner.set(Thread.currentThread());
            return playwright;
        }

        @Override
        protected Connection.Response request(String url, String userAgent, Map<String, String> cookies)
                throws java.io.IOException {
            assertEquals(owner.get(), Thread.currentThread());
            assertEquals("https://anidb.net/", url);
            assertEquals("test-agent", userAgent);
            requestCount++;
            exported = cookies;
            if (networkFailure) throw new java.io.IOException("test network failure");
            return response;
        }
    };

    private void configure() {
        properties.setEnabled(true);
        properties.setProfileDirectory(profiles.toString());
        when(playwright.chromium()).thenReturn(chromium);
        when(chromium.launchPersistentContext(any(Path.class), any())).thenReturn(context);
        when(context.pages()).thenReturn(List.of(page));
        when(page.url()).thenReturn("https://anidb.net/");
        when(page.title()).thenReturn("AniDB");
        when(page.mainFrame()).thenReturn(mainFrame);
        when(page.evaluate("navigator.userAgent")).thenReturn("test-agent");
        when(page.evaluate("document.readyState")).thenReturn("complete");
        when(navigation.request()).thenReturn(navigationRequest);
        when(navigationRequest.isNavigationRequest()).thenReturn(true);
        when(navigationRequest.frame()).thenReturn(mainFrame);
        when(navigation.url()).thenReturn("https://anidb.net/");
        when(navigation.status()).thenReturn(200);
        doAnswer(call -> { responseListener = call.getArgument(0); return null; }).when(page).onResponse(any());
        when(page.navigate(anyString(), any())).thenAnswer(call -> {
            responseListener.accept(navigation);
            return navigation;
        });
        when(context.cookies("https://anidb.net/")).thenReturn(List.of(
                new Cookie("cf_clearance", "test-clearance").setDomain("anidb.net").setPath("/")));
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("<html><title>AniDB</title><main id='normal'>ready</main></html>");
    }

    @AfterEach
    void shutdown() { service.shutdown(); }

    @Test
    void rejectsDisabledAndUnapprovedUrlsWithoutLaunchingBrowser() {
        assertEquals(503, assertThrows(ResponseStatusException.class,
                () -> service.start("https://anidb.net/")).getStatusCode().value());
        properties.setEnabled(true);
        for (String url : List.of("http://anidb.net/", "https://anidb.net.evil.test/",
                "https://user:pass@anidb.net/", "https://anidb.net:8443/", "file:///tmp/test")) {
            assertEquals(400, assertThrows(ResponseStatusException.class,
                    () -> service.start(url)).getStatusCode().value());
        }
        verifyNoInteractions(playwright);
    }

    @Test
    void reusesSameOriginProfileAcrossSessionsAndSeparatesHosts() {
        configure();
        Path profile = service.profilePath("https://anidb.net/");
        assertEquals(profile, service.profilePath("https://ANIDB.NET:443/anime/1?test=value"));
        assertNotEquals(profile, service.profilePath("https://example.test/"));
        assertTrue(profile.startsWith(profiles));
        var first = service.start("https://anidb.net/");
        service.cancel(first.sessionId());
        var second = service.start("https://anidb.net/");
        assertNotEquals(first.sessionId(), second.sessionId());
        verify(chromium, times(2)).launchPersistentContext(eq(profile), any());
        verify(chromium, never()).launch(any());
        service.cancel(second.sessionId());
    }

    @Test
    void statusIsReadOnlyAndJsoupTestDoesNotCloseWindow() {
        configure();
        var session = service.start("https://anidb.net/");
        assertEquals(409, assertThrows(ResponseStatusException.class,
                () -> service.start("https://anidb.net/")).getStatusCode().value());
        var status = service.status(session.sessionId());
        assertEquals(BrowserState.BROWSER_READY, status.browserState());
        assertEquals(JsoupState.NOT_TESTED, status.jsoup().state());
        assertEquals(0, requestCount);
        assertEquals(1, status.navigationCount());
        assertEquals(JsoupState.PASSED, service.testJsoup(session.sessionId()).jsoup().state());
        assertEquals(Map.of("cf_clearance", "test-clearance"), exported);
        verify(page, times(1)).navigate(anyString(), any());
        verify(context, never()).close();
        service.cancel(session.sessionId());
    }

    @Test
    void keepsBrowserReadyWhenJsoupIsBlockedOrNetworkFails() {
        configure();
        var session = service.start("https://anidb.net/");
        when(response.statusCode()).thenReturn(403);
        var status = service.testJsoup(session.sessionId());
        assertEquals(BrowserState.BROWSER_READY, status.browserState());
        assertEquals(JsoupState.FAILED, status.jsoup().state());
        assertEquals(403, status.jsoup().httpStatus());
        assertEquals(409, assertThrows(ResponseStatusException.class,
                () -> service.verify(session.sessionId())).getStatusCode().value());
        networkFailure = true;
        assertNull(service.testJsoup(session.sessionId()).jsoup().httpStatus());
        assertEquals(502, assertThrows(ResponseStatusException.class,
                () -> service.verify(session.sessionId())).getStatusCode().value());
        verify(context, never()).close();
        service.cancel(session.sessionId());
    }

    @Test
    void observesChallengeReturningAfterSuccessWithoutTestingJsoup() {
        configure();
        var session = service.start("https://anidb.net/");
        assertEquals(BrowserState.BROWSER_READY, service.status(session.sessionId()).browserState());
        assertEquals(JsoupState.PASSED, service.testJsoup(session.sessionId()).jsoup().state());
        when(navigation.headerValue("cf-mitigated")).thenReturn("challenge");
        when(navigation.status()).thenReturn(403);
        when(page.title()).thenReturn("Just a moment...");
        // Deliver the response through Playwright's message-loop seam on the owner worker thread.
        doAnswer(call -> { responseListener.accept(navigation); return null; }).when(page).waitForTimeout(anyDouble());
        var status = service.status(session.sessionId());
        assertEquals(BrowserState.CHALLENGED, status.browserState());
        assertTrue(status.challengeResponseCount() > 0);
        assertEquals(JsoupState.NOT_TESTED, service.testJsoup(session.sessionId()).jsoup().state());
        assertEquals(409, assertThrows(ResponseStatusException.class,
                () -> service.verify(session.sessionId())).getStatusCode().value());
        assertEquals(1, requestCount);
        service.cancel(session.sessionId());
    }

    @Test
    void optionalSelectorMustMatchBothBrowserAndJsoup() {
        configure();
        Locator locator = mock(Locator.class);
        when(page.locator("#normal")).thenReturn(locator);
        when(locator.count()).thenReturn(0);
        var session = service.start("https://anidb.net/", "#normal");
        assertEquals(BrowserState.WAITING_FOR_VERIFICATION, service.status(session.sessionId()).browserState());
        assertEquals(0, requestCount);
        when(locator.count()).thenReturn(1);
        when(response.body()).thenReturn("<html><title>AniDB</title></html>");
        assertEquals(JsoupState.FAILED, service.testJsoup(session.sessionId()).jsoup().state());
        when(response.body()).thenReturn("<html><title>AniDB</title><main id='normal'>ready</main></html>");
        assertEquals(JsoupState.PASSED, service.testJsoup(session.sessionId()).jsoup().state());
        service.cancel(session.sessionId());
    }

    @Test
    void missingClearanceAndWrongOriginNeverTriggerJsoup() {
        configure();
        when(context.cookies("https://anidb.net/")).thenReturn(List.of());
        var session = service.start("https://anidb.net/");
        assertEquals(BrowserState.WAITING_FOR_VERIFICATION, service.status(session.sessionId()).browserState());
        assertEquals(JsoupState.NOT_TESTED, service.testJsoup(session.sessionId()).jsoup().state());
        when(page.url()).thenReturn("https://other.test/?token=secret#fragment");
        var status = service.status(session.sessionId());
        assertEquals(BrowserState.WRONG_ORIGIN, status.browserState());
        assertEquals("https://other.test/", status.currentUrl());
        assertEquals(0, requestCount);
        service.cancel(session.sessionId());
    }

    @Test
    void closesOnOwnerThreadAndRetainsTerminalStatus() {
        configure();
        var session = service.start("https://anidb.net/");
        assertNotEquals(Thread.currentThread(), owner.get());
        assertEquals("AniDB", service.verify(session.sessionId()).title());
        doAnswer(call -> { assertEquals(owner.get(), Thread.currentThread()); return null; }).when(context).close();
        service.release(session.sessionId());
        verify(context).close();
        verify(playwright).close();
        assertEquals(BrowserState.SAVED, service.status(session.sessionId()).browserState());
        assertEquals(410, assertThrows(ResponseStatusException.class,
                () -> service.verify(session.sessionId())).getStatusCode().value());
    }

    @Test
    void manuallyClosedWindowReportsClosedAndDoesNotLoseStatus() {
        configure();
        var session = service.start("https://anidb.net/");
        when(page.isClosed()).thenReturn(true);
        assertEquals(BrowserState.WINDOW_CLOSED, service.status(session.sessionId()).browserState());
        assertEquals(BrowserState.WINDOW_CLOSED, service.status(session.sessionId()).browserState());
        assertEquals(410, assertThrows(ResponseStatusException.class,
                () -> service.testJsoup(session.sessionId())).getStatusCode().value());
    }

    @Test
    void blockPageWithNoChallengeHeaderIsNotReportedAsVerification() {
        configure();
        when(navigation.status()).thenReturn(403);
        when(page.title()).thenReturn("Attention Required! | Cloudflare");
        var session = service.start("https://anidb.net/");
        assertEquals(BrowserState.BLOCKED, service.status(session.sessionId()).browserState());
        assertEquals(JsoupState.NOT_TESTED, service.testJsoup(session.sessionId()).jsoup().state());
        assertEquals(0, requestCount);
        service.cancel(session.sessionId());
    }

    @Test
    void invalidReadySelectorDoesNotLaunchBrowser() {
        configure();
        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> service.start("https://anidb.net/", "[broken")).getStatusCode().value());
        verify(chromium, never()).launchPersistentContext(any(Path.class), any());
    }

    @Test
    void rejectsRedirectsBlocksAndChallengeTitlesEvenWith200() {
        assertFalse(BrowserVerificationService.isUsableResponse(302, null, ""));
        assertFalse(BrowserVerificationService.isUsableResponse(403, null, "AniDB"));
        assertFalse(BrowserVerificationService.isUsableResponse(200, null, "Just a moment..."));
        assertFalse(BrowserVerificationService.isUsableResponse(200, null, "Attention Required! | Cloudflare"));
        assertFalse(BrowserVerificationService.isUsableResponse(200, "challenge", "AniDB"));
        assertTrue(BrowserVerificationService.isUsableResponse(200, null, "AniDB"));
    }
}
