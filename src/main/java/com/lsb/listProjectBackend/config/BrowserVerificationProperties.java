package com.lsb.listProjectBackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "spider.browser-verification")
public class BrowserVerificationProperties {
    private boolean enabled = false;
    // Empty means Playwright's bundled Chromium. Chrome/Edge must already be installed.
    private String channel = "chrome";
    private List<String> allowedHosts = List.of("anidb.net");
    private int sessionTimeoutSeconds = 600;
    private String profileDirectory = ".browser-verification/profiles";
}
