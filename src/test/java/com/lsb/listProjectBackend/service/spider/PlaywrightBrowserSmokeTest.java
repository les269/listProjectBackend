package com.lsb.listProjectBackend.service.spider;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;
import com.microsoft.playwright.options.Cookie;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Opt-in installation check: no external requests, no manual challenge and no database writes. */
@EnabledIfEnvironmentVariable(named = "PLAYWRIGHT_SMOKE_TEST", matches = "true")
class PlaywrightBrowserSmokeTest {
    @TempDir
    Path profile;

    @Test
    void launchesInstalledChromeAndReadsPage() {
        try (Playwright playwright = Playwright.create(new Playwright.CreateOptions()
                .setEnv(Map.of("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1")));
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                     .setChannel("chrome").setHeadless(true))) {
            Page page = browser.newPage();
            page.setContent("<html><title>Browser verification smoke test</title><body>ready</body></html>");
            assertEquals("Browser verification smoke test", page.title());
            assertTrue(((String) page.evaluate("navigator.userAgent")).contains("Chrome/"));
        }
    }

    @Test
    void persistentProfileRetainsCookiesAndLocalStorageAcrossLaunches() {
        try (Playwright playwright = Playwright.create(new Playwright.CreateOptions()
                .setEnv(Map.of("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1")))) {
            var options = new BrowserType.LaunchPersistentContextOptions().setChannel("chrome").setHeadless(true);
            try (BrowserContext first = playwright.chromium().launchPersistentContext(profile, options)) {
                // Fulfilled locally: the fake site is never contacted.
                first.route("https://profile.test/**", route -> route.fulfill(new Route.FulfillOptions()
                        .setStatus(200).setContentType("text/html").setBody("<title>Profile test</title>")));
                Page page = first.newPage();
                page.navigate("https://profile.test/");
                first.addCookies(List.of(new Cookie("profile_test", "retained")
                        .setUrl("https://profile.test/").setExpires(System.currentTimeMillis() / 1000.0 + 3600)));
                page.evaluate("localStorage.setItem('profile-test', 'retained')");
            }
            try (BrowserContext second = playwright.chromium().launchPersistentContext(profile, options)) {
                second.route("https://profile.test/**", route -> route.fulfill(new Route.FulfillOptions()
                        .setStatus(200).setContentType("text/html").setBody("<title>Profile test</title>")));
                Page page = second.newPage();
                page.navigate("https://profile.test/");
                assertTrue(second.cookies("https://profile.test/").stream()
                        .anyMatch(cookie -> cookie.name.equals("profile_test") && cookie.value.equals("retained")));
                assertEquals("retained", page.evaluate("localStorage.getItem('profile-test')"));
            }
        }
    }
}
