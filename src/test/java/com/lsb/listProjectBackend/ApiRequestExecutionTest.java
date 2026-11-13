package com.lsb.listProjectBackend;

import com.lsb.listProjectBackend.domain.connection.*;
import com.lsb.listProjectBackend.service.connection.ApiRequestConfigServiceImpl;
import com.lsb.listProjectBackend.utils.Global;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ApiRequestExecutionTest {
    private final LinkedBlockingQueue<Received> received = new LinkedBlockingQueue<>();
    private HttpServer server;
    private volatile int status = 200;
    private volatile String response = "[{\"id\":\"one\"}]";
    private volatile String responseContentType = "application/json";
    private volatile long delay;
    private ApiRequestSettings settings;

    record Received(String method, String query, String contentType, String body, String path) {}

    @BeforeEach
    void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            try (exchange) {
                received.add(new Received(exchange.getRequestMethod(), exchange.getRequestURI().getRawQuery(),
                        exchange.getRequestHeaders().getFirst("Content-Type"),
                        new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8),
                        exchange.getRequestURI().getRawPath()));
                if (delay > 0) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", responseContentType);
                exchange.sendResponseHeaders(status, status == 204 ? -1 : bytes.length);
                if (status != 204) {
                    exchange.getResponseBody().write(bytes);
                }
            }
        });
        server.start();
        settings = new ApiRequestSettings();
        settings.setEndpointUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/");
        settings.setHttpMethod(Global.HttpMethodType.post);
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private List<java.util.Map<String, Object>> call() throws Exception {
        var result = callRaw();
        return result.isNull() ? List.of() : new tools.jackson.databind.ObjectMapper().convertValue(result,
                new tools.jackson.core.type.TypeReference<List<java.util.Map<String, Object>>>() {});
    }

    private tools.jackson.databind.JsonNode callRaw() throws Exception {
        return savedCaller().singalCallApi(new SingalCallApiReqTO(
                "saved-api", new tools.jackson.databind.ObjectMapper().createObjectNode()));
    }

    private Received request() throws Exception {
        return java.util.Objects.requireNonNull(received.poll(2, TimeUnit.SECONDS));
    }

    @Test
    void singleCallLoadsSavedSettingsAndUsesSuppliedJsonWithoutChangingThem() throws Exception {
        var repository = org.mockito.Mockito.mock(
                com.lsb.listProjectBackend.repository.dynamic.connection.ApiRequestConfigRepository.class);
        var mapper = org.mockito.Mockito.mock(
                com.lsb.listProjectBackend.mapper.connection.ApiRequestConfigMapper.class);
        var entity = new com.lsb.listProjectBackend.entity.dynamic.common.ApiRequestConfig();
        settings.setBodyType(Global.ApiRequestBodyType.text);
        settings.setRequestBody("saved body");
        settings.setHttpParams(List.of(param("saved", "yes", true)));
        var stored = new ApiRequestConfigTO("saved-api", "Saved", settings, null, null);
        org.mockito.Mockito.when(repository.findById("saved-api")).thenReturn(java.util.Optional.of(entity));
        org.mockito.Mockito.when(mapper.toDomain(entity)).thenReturn(stored);
        var caller = new ApiRequestConfigServiceImpl(repository, mapper);
        var request = new tools.jackson.databind.ObjectMapper().readValue("""
                {"apiId":"saved-api","json":{"name":"中文","nested":[1,true]}}
                """, SingalCallApiReqTO.class);

        assertEquals("one", caller.singalCallApi(request).get(0).get("id").asText());
        var sent = request();
        assertEquals("POST", sent.method());
        assertEquals("saved=yes", sent.query());
        assertTrue(sent.contentType().startsWith("text/plain"));
        assertEquals("saved body", sent.body());
        assertEquals("saved body", settings.getRequestBody());
        assertEquals(Global.ApiRequestBodyType.text, settings.getBodyType());
        org.mockito.Mockito.verify(repository).findById("saved-api");
        org.mockito.Mockito.verifyNoMoreInteractions(repository);
    }

    @Test
    void singleCallResolvesUrlQueryAndBodyTemplatesWithoutChangingSavedSettings() throws Exception {
        settings.setEndpointUrl(settings.getEndpointUrl() + "items/{{$.id}}?id=old&keep=yes");
        settings.setBodyType(Global.ApiRequestBodyType.text);
        settings.setRequestBody("id={{$.id}}, name={{$.user.name}}");
        settings.setHttpParams(List.of(param("id", "{{$.id}}", true), param("{{$.key}}", "{{$.user.name}}", true)));
        var caller = savedCaller();
        var input = new SingalCallApiReqTO("saved-api", new tools.jackson.databind.ObjectMapper().readTree(
                "{\"id\":\"a &/\",\"key\":\"name\",\"user\":{\"name\":\"test\"}}"));
        caller.singalCallApi(input);
        var sent = request();
        assertEquals("/items/a%20%26%2F", sent.path());
        assertEquals("keep=yes&id=a%20%26%2F&name=test", sent.query());
        assertEquals("id=a &/, name=test", sent.body());
        assertTrue(sent.contentType().startsWith("text/plain"));
        assertEquals("id={{$.id}}, name={{$.user.name}}", settings.getRequestBody());
        assertEquals("{{$.id}}", settings.getHttpParams().getFirst().getValue());

        settings.setHttpMethod(Global.HttpMethodType.get);
        caller.singalCallApi(input);
        var get = request();
        assertEquals("keep=yes&id=a%20%26%2F&name=test", get.query());
        assertEquals("", get.body());
    }

    @Test
    void singleCallResolvesJsonAndBothFormBodyTemplates() throws Exception {
        var caller = savedCaller();
        var input = new SingalCallApiReqTO("saved-api", new tools.jackson.databind.ObjectMapper()
                .readTree("{\"id\":123,\"key\":\"title\",\"name\":\"hello world\"}"));
        settings.setBodyType(Global.ApiRequestBodyType.json);
        settings.setRequestBody("{\"id\":{{$.id}},\"name\":\"{{$.name}}\"}");
        caller.singalCallApi(input);
        assertEquals(new tools.jackson.databind.ObjectMapper().readTree("{\"id\":123,\"name\":\"hello world\"}"),
                new tools.jackson.databind.ObjectMapper().readTree(request().body()));

        settings.setBodyFormFields(List.of(param("{{$.key}}", "{{$.name}}", true)));
        settings.setBodyType(Global.ApiRequestBodyType.formUrlEncoded);
        caller.singalCallApi(input);
        assertEquals("title=hello+world", request().body());
        settings.setBodyType(Global.ApiRequestBodyType.formData);
        caller.singalCallApi(input);
        var multipart = request();
        assertTrue(multipart.body().contains("name=\"title\""));
        assertTrue(multipart.body().contains("hello world"));
        assertEquals("{{$.key}}", settings.getBodyFormFields().getFirst().getName());
        assertEquals("{{$.name}}", settings.getBodyFormFields().getFirst().getValue());
    }

    private ApiRequestConfigServiceImpl savedCaller() {
        var repository = org.mockito.Mockito.mock(
                com.lsb.listProjectBackend.repository.dynamic.connection.ApiRequestConfigRepository.class);
        var mapper = org.mockito.Mockito.mock(
                com.lsb.listProjectBackend.mapper.connection.ApiRequestConfigMapper.class);
        var entity = new com.lsb.listProjectBackend.entity.dynamic.common.ApiRequestConfig();
        org.mockito.Mockito.when(repository.findById("saved-api")).thenReturn(java.util.Optional.of(entity));
        org.mockito.Mockito.when(mapper.toDomain(entity))
                .thenReturn(new ApiRequestConfigTO("saved-api", "", settings, null, null));
        return new ApiRequestConfigServiceImpl(repository, mapper);
    }

    @Test
    void singleCallRejectsUnknownIdsWithoutSendingRequest() {
        var repository = org.mockito.Mockito.mock(
                com.lsb.listProjectBackend.repository.dynamic.connection.ApiRequestConfigRepository.class);
        var mapper = org.mockito.Mockito.mock(
                com.lsb.listProjectBackend.mapper.connection.ApiRequestConfigMapper.class);
        org.mockito.Mockito.when(repository.findById("missing")).thenReturn(java.util.Optional.empty());
        var caller = new ApiRequestConfigServiceImpl(repository, mapper);
        var error = assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> caller.singalCallApi(new SingalCallApiReqTO("missing",
                        new tools.jackson.databind.ObjectMapper().readTree("{}"))));
        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, error.getStatusCode());
        assertTrue(received.isEmpty());
    }

    @Test
    void executesEveryMethodAndReadsTheirResponses() throws Exception {
        for (var method : Global.HttpMethodType.values()) {
            settings.setHttpMethod(method);
            assertEquals("one", call().getFirst().get("id"));
            assertEquals(method.name().toUpperCase(java.util.Locale.ROOT), request().method());
        }
    }

    @Test
    void getUsesQueryParametersWithoutSendingConfiguredBody() throws Exception {
        settings.setHttpMethod(Global.HttpMethodType.get);
        settings.setBodyType(Global.ApiRequestBodyType.json);
        settings.setRequestBody("{\"ignored\":true}");
        settings.setHttpParams(List.of(param("q", "value", true)));
        call();
        var sent = request();
        assertEquals("GET", sent.method());
        assertEquals("q=value", sent.query());
        assertEquals("", sent.body());
        assertNull(sent.contentType());
    }

    @Test
    void replacesExistingUrlParametersWithEnabledConfiguredValues() throws Exception {
        settings.setEndpointUrl(settings.getEndpointUrl()
                + "?headerId=ThemeHeader:av-list,1,imageList&header%49d=old&keep=a%20b&disabled=keep#fragment");
        settings.setHttpParams(List.of(param("headerId", "ThemeHeader:av-list,1,imageList", true),
                param("disabled", "ignored", false)));
        call();
        assertEquals("keep=a%20b&disabled=keep&headerId=ThemeHeader%3Aav-list%2C1%2CimageList",
                request().query());
    }

    @Test
    void replacesUrlValuesWhilePreservingExplicitDuplicateSettings() throws Exception {
        settings.setEndpointUrl(settings.getEndpointUrl() + "?tag=old&tag=older");
        settings.setHttpParams(List.of(param("tag", "one", true), param("tag", "two", true)));
        call();
        assertEquals("tag=one&tag=two", request().query());

        settings.setHttpParams(List.of());
        call();
        assertEquals("tag=old&tag=older", request().query());
    }

    @Test
    void sendsEmptyQueryValuesAndPreservesDuplicatesAndExistingEncoding() throws Exception {
        settings.setEndpointUrl(settings.getEndpointUrl() + "?existing=a%20b#fragment");
        var flag = param("flag", "", true);
        settings.setHttpParams(List.of(flag, param("dup", "", true), param("dup", "中 &+=", true),
                param("disabled", "x", false)));
        call();
        assertEquals("existing=a%20b&flag=&dup=&dup=%E4%B8%AD%20%26%2B%3D", request().query());
    }

    @Test
    void sendsEveryRawFormatAsUtf8WithoutReencodingTheBody() throws Exception {
        String[][] formats = {{"text", "text/plain"}, {"javascript", "application/javascript"},
                {"json", "application/json"}, {"html", "text/html"}, {"xml", "application/xml"}};
        for (var format : formats) {
            settings.setBodyType(Global.ApiRequestBodyType.valueOf(format[0]));
            settings.setRequestBody("中 \"raw\"\n");
            call();
            var sent = request();
            assertTrue(sent.contentType().startsWith(format[1]));
            assertEquals(settings.getRequestBody(), sent.body());
        }
        settings.setBodyType(null);
        settings.setRequestBody("{\"legacy\":true}");
        call();
        assertTrue(request().contentType().startsWith("application/json"));
        settings.setBodyType(Global.ApiRequestBodyType.none);
        call();
        assertEquals("", request().body());
    }

    @Test
    void sendsEnabledDuplicateFormFieldsInBothFormFormats() throws Exception {
        settings.setBodyFormFields(List.of(param("title", "中 &", true), param("title", "two", true),
                param("disabled", "secret", false)));
        settings.setBodyType(Global.ApiRequestBodyType.formUrlEncoded);
        call();
        var encoded = request();
        assertTrue(encoded.contentType().startsWith("application/x-www-form-urlencoded"));
        assertEquals("title=%E4%B8%AD+%26&title=two", encoded.body());
        settings.setBodyType(Global.ApiRequestBodyType.formData);
        call();
        var multipart = request();
        assertTrue(multipart.contentType().startsWith("multipart/form-data;boundary="));
        assertTrue(multipart.body().contains("name=\"title\""));
        assertTrue(multipart.body().contains("中 &"));
        assertTrue(multipart.body().contains("two"));
        assertFalse(multipart.body().contains("disabled"));
    }

    @Test
    void skipsDisabledRequestsAndHandlesNoContent() throws Exception {
        settings.setEnabled(false);
        assertTrue(call().isEmpty());
        assertTrue(received.isEmpty());
        settings.setEnabled(null);
        status = 204;
        assertTrue(call().isEmpty());
        request();
    }

    @Test
    void propagatesHttpErrorsAndRejectsMalformedJson() {
        status = 400;
        assertThrows(HttpClientErrorException.class, this::call);
        status = 200;
        response = "not json";
        assertThrows(tools.jackson.core.JacksonException.class, this::callRaw);
    }

    @Test
    void acceptsSingleObjectResponseAndPreservesNestedData() throws Exception {
        response = "{\"id\":\"one\",\"data\":{\"tags\":[\"a\",\"b\"]},\"optional\":null}";
        var result = callRaw();
        assertTrue(result.isObject());
        assertEquals("one", result.get("id").asText());
        assertEquals(new tools.jackson.databind.ObjectMapper().readTree("{\"tags\":[\"a\",\"b\"]}"),
                result.get("data"));
        assertTrue(result.has("optional"));
        assertTrue(result.get("optional").isNull());
    }

    @Test
    void acceptsEmptyObjectsArraysAndJsonNull() throws Exception {
        response = "{}";
        assertTrue(callRaw().isObject());
        response = "[]";
        assertTrue(call().isEmpty());
        response = "null";
        assertTrue(callRaw().isNull());
    }

    @Test
    void acceptsPlainTextResponsesAsStrings() throws Exception {
        responseContentType = "text/plain;charset=UTF-8";
        response = "hello world";
        assertEquals("hello world", callRaw().asText());
        response = "42";
        assertTrue(callRaw().isString());
    }

    @Test
    void preservesScalarAndMixedArrayResponseTypes() throws Exception {
        var json = new tools.jackson.databind.ObjectMapper();
        for (String value : List.of("\"hello\"", "true", "false", "42", "1.5",
                "[1,true,\"text\",null,{\"id\":1}]")) {
            response = value;
            var result = callRaw();
            assertEquals(json.readTree(value), result);
            assertEquals(json.readTree(value), json.readTree(json.writeValueAsString(result)));
        }
    }

    @Test
    void enforcesConfiguredReadTimeoutAndValidatesRange() {
        delay = 300;
        settings.setTimeoutMs(30L);
        assertThrows(ResourceAccessException.class, this::call);
        settings.setTimeoutMs(-1L);
        assertThrows(IllegalArgumentException.class, this::call);
        settings.setTimeoutMs((long) Integer.MAX_VALUE + 1);
        assertThrows(IllegalArgumentException.class, this::call);
    }

    private ApiRequestQueryParam param(String name, String value, boolean enabled) {
        var param = new ApiRequestQueryParam();
        param.setName(name);
        param.setValue(value);
        param.setEnabled(enabled);
        return param;
    }
}
