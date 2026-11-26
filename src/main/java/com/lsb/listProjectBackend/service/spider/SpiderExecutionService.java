package com.lsb.listProjectBackend.service.spider;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.lsb.listProjectBackend.domain.common.CookieListTO;
import com.lsb.listProjectBackend.domain.common.HeadersTO;
import com.lsb.listProjectBackend.domain.common.LsbException;
import com.lsb.listProjectBackend.entity.dynamic.common.Cookie;
import com.lsb.listProjectBackend.entity.dynamic.common.Header;
import com.lsb.listProjectBackend.entity.dynamic.spider.ExtractionRule;
import com.lsb.listProjectBackend.entity.dynamic.spider.ValuePipeline;
import com.lsb.listProjectBackend.entity.dynamic.spider.ValuePipelineContext;
import com.lsb.listProjectBackend.service.common.CookieListService;
import com.lsb.listProjectBackend.service.common.HeadersService;
import com.lsb.listProjectBackend.utils.ValuePipelineUtils;
import com.lsb.listProjectBackend.utils.Global;
import com.lsb.listProjectBackend.utils.JsonUtils;
import com.lsb.listProjectBackend.utils.Utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jsoup.Connection;
import org.jsoup.Connection.Request;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Shared request and extraction operations for item and pagination spiders. */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpiderExecutionService {
    private final CookieListService cookieListService;
    private final ExtractionRuleService extractionRuleService;
    private final HeadersService headersService;
    private final ReplaceValueMapLoader replaceValueMapLoader;

    public Map<String, Map<String, Object>> loadReplaceValueMaps(List<ExtractionRule> rules,
            List<HeadersTO> headers, List<CookieListTO> cookies) {
        var pipelines = new ArrayList<ValuePipeline>(extractionRuleService.allPipelines(rules));
        for (var header : headers) {
            if (header != null && header.list() != null) {
                for (var entry : header.list()) {
                    if (entry.getValuePipelines() != null) {
                        pipelines.addAll(entry.getValuePipelines());
                    }
                }
            }
        }
        for (var cookie : cookies) {
            if (cookie != null && cookie.list() != null) {
                for (var entry : cookie.list()) {
                    if (entry.getValuePipelines() != null) {
                        pipelines.addAll(entry.getValuePipelines());
                    }
                }
            }
        }
        return replaceValueMapLoader.load(pipelines);
    }

    public void executeRequest(String url, Global.ExtractionRuleMode mode, List<ExtractionRule> extractionRules,
            HeadersTO headers, CookieListTO cookies, DocumentContext result, Long requestIntervalMs)
            throws IOException, InterruptedException {
        executeRequest(url, mode, extractionRules, headers, cookies, result, requestIntervalMs, List.of());
    }

    public void executeRequest(String url, Global.ExtractionRuleMode mode, List<ExtractionRule> extractionRules,
            HeadersTO headers, CookieListTO cookies, DocumentContext result, Long requestIntervalMs,
            List<String> initialKeys) throws IOException, InterruptedException {
        var maps = loadReplaceValueMaps(extractionRules, headers == null ? List.of() : List.of(headers),
                cookies == null ? List.of() : List.of(cookies));
        executeRequest(url, mode, extractionRules, headers, cookies, result, requestIntervalMs, initialKeys, maps);
    }

    public void executeRequest(String url, Global.ExtractionRuleMode mode, List<ExtractionRule> extractionRules,
            HeadersTO headers, CookieListTO cookies, DocumentContext result, Long requestIntervalMs,
            List<String> initialKeys,
            Map<String, Map<String, Object>> replaceValueMaps) throws IOException, InterruptedException {
        if (Utils.isBlank(url)) {
            Map<String, String> headerMap = headers == null ? Map.of() : getHeadersMap(headers.list(), result, replaceValueMaps);
            Map<String, String> cookieMap = cookies == null ? Map.of() : getCookieMap(cookies.list(), result, replaceValueMaps);
            switch (mode) {
                case SELECT -> useCssSelect("<html><head></head><body></body></html>", cookieMap, headerMap,
                        extractionRules, result, initialKeys, replaceValueMaps);
                case JSON_PATH -> useJsonPath("{}", cookieMap, headerMap, extractionRules, result, initialKeys, replaceValueMaps);
            }
            return;
        }
        Connection connection = getConnection(url);
        setupHeaders(connection, headers, result, replaceValueMaps);
        setupCookies(connection, cookies, result, replaceValueMaps);
        waitForRequestInterval(requestIntervalMs);
        useExtraction(connection, mode, extractionRules, result, initialKeys, replaceValueMaps);
    }

    public void waitForRequestInterval(Long requestIntervalMs) throws InterruptedException {
        if (requestIntervalMs == null || requestIntervalMs <= 0) {
            return;
        }
        try {
            Thread.sleep(requestIntervalMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    public void useExtraction(Connection connection, Global.ExtractionRuleMode mode,
            List<ExtractionRule> extractionRules, DocumentContext result) throws IOException {
        useExtraction(connection, mode, extractionRules, result, List.of());
    }

    public void useExtraction(Connection connection, Global.ExtractionRuleMode mode,
            List<ExtractionRule> extractionRules, DocumentContext result, List<String> initialKeys) throws IOException {
        useExtraction(connection, mode, extractionRules, result, initialKeys, loadReplaceValueMaps(extractionRules, List.of(), List.of()));
    }

    public void useExtraction(Connection connection, Global.ExtractionRuleMode mode,
            List<ExtractionRule> extractionRules, DocumentContext result, List<String> initialKeys,
            Map<String, Map<String, Object>> replaceValueMaps) throws IOException {
        Document doc;
        Request req;
        switch (mode) {
            case Global.ExtractionRuleMode.SELECT:
                doc = connection.get();
                req = connection.request();
                useCssSelect(doc.html(), req.cookies(), req.headers(), extractionRules, result, initialKeys, replaceValueMaps);
                break;
            case Global.ExtractionRuleMode.JSON_PATH:
                doc = connection.ignoreContentType(true).get();
                req = connection.request();
                useJsonPath(doc.text(), req.cookies(), req.headers(), extractionRules, result, initialKeys, replaceValueMaps);
                break;
        }
    }

    public void useCssSelect(String html, Map<String, String> cookies, Map<String, String> headers,
            List<ExtractionRule> extractionRules,
            DocumentContext result) {
        useCssSelect(html, cookies, headers, extractionRules, result, List.of());
    }

    public void useCssSelect(String html, Map<String, String> cookies, Map<String, String> headers,
            List<ExtractionRule> extractionRules, DocumentContext result, List<String> initialKeys) {
        useCssSelect(html, cookies, headers, extractionRules, result, initialKeys, loadReplaceValueMaps(extractionRules, List.of(), List.of()));
    }

    public void useCssSelect(String html, Map<String, String> cookies, Map<String, String> headers,
            List<ExtractionRule> extractionRules, DocumentContext result, List<String> initialKeys,
            Map<String, Map<String, Object>> replaceValueMaps) {
        try {
            Document doc = Jsoup.parse(html);
            for (ExtractionRule extractionRule : extractionRuleService.sortedRules(extractionRules)) {
                if (!extractionRuleService.checkCondition(extractionRule, result)) {
                    continue;
                }
                var pipelines = extractionRule.getPipelines();
                Elements elements = Utils.isNotBlank(extractionRule.getSelector())
                        ? doc.select(extractionRule.getSelector())
                        : null;
                var context = ValuePipelineContext.builder()
                        .result(result)
                        .elements(elements)
                        .initialKeys(initialKeys)
                        .replaceValueMaps(replaceValueMaps)
                        .cookies(cookies != null ? cookies : new HashMap<>())
                        .headers(headers != null ? headers : new HashMap<>())
                        .build();
                Object pipelineValue = ValuePipelineUtils.applyPipelines(pipelines, null, context);
                if (elements != null && !ValuePipelineUtils.hasEnabledPipelines(pipelines)) {
                    pipelineValue = elements.stream().map(Element::text).toList();
                }
                JsonUtils.putValue(result, extractionRule.getKey(),
                        ValuePipelineUtils.normalizeExtractedValue(pipelineValue));
            }
        } catch (Exception e) {
            log.error("css select error:", e);
            throw new LsbException(e.getMessage(), e);
        }
    }

    public void useJsonPath(String json, Map<String, String> cookies, Map<String, String> headers,
            List<ExtractionRule> extractionRules,
            DocumentContext result) {
        useJsonPath(json, cookies, headers, extractionRules, result, List.of());
    }

    public void useJsonPath(String json, Map<String, String> cookies, Map<String, String> headers,
            List<ExtractionRule> extractionRules, DocumentContext result, List<String> initialKeys) {
        useJsonPath(json, cookies, headers, extractionRules, result, initialKeys, loadReplaceValueMaps(extractionRules, List.of(), List.of()));
    }

    public void useJsonPath(String json, Map<String, String> cookies, Map<String, String> headers,
            List<ExtractionRule> extractionRules, DocumentContext result, List<String> initialKeys,
            Map<String, Map<String, Object>> replaceValueMaps) {
        for (ExtractionRule extractionRule : extractionRuleService.sortedRules(extractionRules)) {
            if (!extractionRuleService.checkCondition(extractionRule, result)) {
                continue;
            }
            var ctx = JsonPath.parse(json);
            var pipelines = extractionRule.getPipelines();
            Object pipelineValue = JsonUtils.getValueOrNull(ctx, extractionRule.getJsonPath());
            log.info("pipeline value before pipeline: {}", pipelineValue);
            var context = ValuePipelineContext.builder()
                    .result(result)
                    .initialKeys(initialKeys)
                    .replaceValueMaps(replaceValueMaps)
                    .cookies(cookies != null ? cookies : new HashMap<>())
                    .headers(headers != null ? headers : new HashMap<>())
                    .build();
            pipelineValue = ValuePipelineUtils.applyPipelines(pipelines, pipelineValue, context);
            JsonUtils.putValue(result, extractionRule.getKey(),
                    ValuePipelineUtils.normalizeExtractedValue(pipelineValue));
        }
    }

    public Connection getConnection(String url) {
        return Jsoup.connect(url).header("Accept-Language", "zh-TW,zh;q=0.9,en-US;q=0.8,en;q=0.7,zh-CN;q=0.6,ja;q=0.5")
                .header("Accept", "*/*").header("Content-Type", "text/html; charset=UTF-8");
    }

    public Map<String, String> getCookieMapByRefId(String refId, Global.CookieListMapType type) {
        var cookieListTO = cookieListService.getByRefIdAndType(refId, type);
        if (cookieListTO != null) {
            return getCookieMap(cookieListTO.list(), null, loadReplaceValueMaps(List.of(), List.of(), List.of(cookieListTO)));
        }
        return Map.of();
    }

    public Map<String, String> getCookieMap(List<Cookie> list, DocumentContext extraContext,
            Map<String, Map<String, Object>> replaceValueMaps) {
        DocumentContext cookiesContext = JsonPath.parse("{}");
        for (Cookie cookie : list) {
            var ctx = ValuePipelineContext.builder().result(cookiesContext).extraContext(extraContext)
                    .replaceValueMaps(replaceValueMaps)
                    .build();
            Object val = ValuePipelineUtils.applyPipelines(cookie.getValuePipelines(), cookie.getValue(), ctx);
            cookiesContext.put("$", cookie.getName(), val != null ? String.valueOf(val) : "");
        }
        return cookiesContext.read("$");
    }

    public Map<String, String> getHeadersMapByRefId(String refId, Global.HeadersMapType type) {
        var headersMapTO = headersService.getByRefIdAndType(refId, type);
        if (headersMapTO != null) {
            return getHeadersMap(headersMapTO.list(), null, loadReplaceValueMaps(List.of(), List.of(headersMapTO), List.of()));
        }
        return Map.of();
    }

    public Map<String, String> getHeadersMap(List<Header> list, DocumentContext extraContext,
            Map<String, Map<String, Object>> replaceValueMaps) {
        DocumentContext headersContext = JsonPath.parse("{}");
        for (Header header : list) {
            var ctx = ValuePipelineContext.builder().result(headersContext).extraContext(extraContext)
                    .replaceValueMaps(replaceValueMaps)
                    .build();
            Object val = ValuePipelineUtils.applyPipelines(header.getValuePipelines(), header.getValue(), ctx);
            headersContext.put("$", header.getName(), val != null ? String.valueOf(val) : "");
        }
        return headersContext.read("$");
    }

    public void setupCookies(Connection connection, CookieListTO to, DocumentContext extraContext) {
        setupCookies(connection, to, extraContext, loadReplaceValueMaps(List.of(), List.of(), to == null ? List.of() : List.of(to)));
    }

    public void setupCookies(Connection connection, CookieListTO to, DocumentContext extraContext,
            Map<String, Map<String, Object>> replaceValueMaps) {
        if (to != null) {
            Map<String, String> cookieMap = getCookieMap(to.list(), extraContext, replaceValueMaps);
            connection.cookies(cookieMap);
        }
    }

    public void setupHeaders(Connection connection, HeadersTO to, DocumentContext extraContext) {
        setupHeaders(connection, to, extraContext, loadReplaceValueMaps(List.of(), to == null ? List.of() : List.of(to), List.of()));
    }

    public void setupHeaders(Connection connection, HeadersTO to, DocumentContext extraContext,
            Map<String, Map<String, Object>> replaceValueMaps) {
        if (to != null) {
            Map<String, String> headersMap = getHeadersMap(to.list(), extraContext, replaceValueMaps);
            connection.headers(headersMap);
        }
    }
}
