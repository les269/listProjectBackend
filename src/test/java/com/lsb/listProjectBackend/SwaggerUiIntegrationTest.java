package com.lsb.listProjectBackend;

import com.jayway.jsonpath.JsonPath;
import com.lsb.listProjectBackend.controller.spider.BrowserVerificationController;
import com.lsb.listProjectBackend.service.spider.BrowserVerificationService;
import com.lsb.listProjectBackend.service.spider.BrowserVerificationStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/** Exercise the actual UI and generated spec without loading application databases or opening Chrome. */
@SpringBootTest(classes = SwaggerUiIntegrationTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SwaggerUiIntegrationTest {
    @LocalServerPort
    private int port;

    @Test
    void servesSwaggerUiAndBrowserVerificationExamplesWithContextPath() throws Exception {
        try (HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10)).build()) {
            String base = "http://localhost:" + port + "/list-project-backend";
            var ui = client.send(HttpRequest.newBuilder(URI.create(base + "/swagger-ui.html"))
                    .timeout(Duration.ofSeconds(20)).GET().build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(200, ui.statusCode());
            assertTrue(ui.body().contains("Swagger UI"));

            var spec = client.send(HttpRequest.newBuilder(URI.create(base + "/v3/api-docs"))
                    .timeout(Duration.ofSeconds(20)).GET().build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(200, spec.statusCode());
            var document = JsonPath.parse(spec.body());
            assertEquals("1. 開啟驗證瀏覽器", document.read(
                    "$.paths['/api/spider/browser-verification'].post.summary"));
            assertEquals(List.of("Browser verification"), document.read(
                    "$.paths['/api/spider/browser-verification'].post.tags"));
            assertEquals("https://anidb.net/", document.read(
                    "$.components.schemas.BrowserVerificationStartRequest.properties.url.example"));
            assertEquals(List.of("SPIDER", "SPIDER_PAGINATION"), document.read(
                    "$.components.schemas.BrowserVerificationCompleteRequest.properties.type.enum"));
            assertNotNull(document.read("$.paths['/api/spider/browser-verification/{sessionId}/complete'].post"));
            assertNotNull(document.read("$.paths['/api/spider/browser-verification/{sessionId}'].delete"));
            assertNotNull(document.read("$.paths['/api/spider/browser-verification/{sessionId}/status'].get"));
            assertNotNull(document.read("$.paths['/api/spider/browser-verification/{sessionId}/test-jsoup'].post"));
            assertTrue(document.<List<String>>read(
                    "$.components.schemas.BrowserVerificationStatus.properties.browserState.enum").contains("BROWSER_READY"));
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration(excludeName = {
            "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
            "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration",
            "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration"
    })
    @Import(BrowserVerificationController.class)
    static class TestApplication {
        @Bean
        BrowserVerificationService browserVerificationService() {
            return mock(BrowserVerificationService.class);
        }

        @Bean
        BrowserVerificationStorageService browserVerificationStorageService() {
            return mock(BrowserVerificationStorageService.class);
        }
    }
}
