package com.lsb.listProjectBackend.service.spider;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.lsb.listProjectBackend.domain.common.CookieListTO;
import com.lsb.listProjectBackend.domain.common.HeadersTO;
import com.lsb.listProjectBackend.domain.common.LsbException;
import com.lsb.listProjectBackend.entity.dynamic.common.Cookie;
import com.lsb.listProjectBackend.entity.dynamic.common.Header;
import com.lsb.listProjectBackend.entity.dynamic.spider.ExtractionRule;
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

    public void executeRequest(String url, Global.ExtractionRuleMode mode, List<ExtractionRule> extractionRules,
            HeadersTO headers, CookieListTO cookies, DocumentContext result, Long requestIntervalMs)
            throws IOException, InterruptedException {
        Connection connection = getConnection(url);
        setupHeaders(connection, headers, result);
        setupCookies(connection, cookies, result);
        waitForRequestInterval(requestIntervalMs);
        useExtraction(connection, mode, extractionRules, result);
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
        Document doc;
        Request req;
        switch (mode) {
            case Global.ExtractionRuleMode.SELECT:
                doc = connection.get();
                req = connection.request();
                useCssSelect(doc.html(), req.cookies(), req.headers(), extractionRules, result);
                break;
            case Global.ExtractionRuleMode.JSON_PATH:
                doc = connection.ignoreContentType(true).get();
                req = connection.request();
                useJsonPath(doc.text(), req.cookies(), req.headers(), extractionRules, result);
                break;
        }
    }

    public void useCssSelect(String html, Map<String, String> cookies, Map<String, String> headers,
            List<ExtractionRule> extractionRules,
            DocumentContext result) {
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
        for (ExtractionRule extractionRule : extractionRuleService.sortedRules(extractionRules)) {
            if (!extractionRuleService.checkCondition(extractionRule, result)) {
                continue;
            }
            var pipelines = extractionRule.getPipelines();
            Object pipelineValue = JsonPath.read(json, extractionRule.getJsonPath());
            log.info("pipeline value before pipeline: {}", pipelineValue);
            var context = ValuePipelineContext.builder()
                    .result(result)
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
            return getCookieMap(cookieListTO.list(), null);
        }
        return Map.of();
    }

    private Map<String, String> getCookieMap(List<Cookie> list, DocumentContext extraContext) {
        DocumentContext cookiesContext = JsonPath.parse("{}");
        for (Cookie cookie : list) {
            var ctx = ValuePipelineContext.builder().result(cookiesContext).extraContext(extraContext)
                    .build();
            Object val = ValuePipelineUtils.applyPipelines(cookie.getValuePipelines(), cookie.getValue(), ctx);
            cookiesContext.put("$", cookie.getName(), val != null ? String.valueOf(val) : "");
        }
        return cookiesContext.read("$");
    }

    public Map<String, String> getHeadersMapByRefId(String refId, Global.HeadersMapType type) {
        var headersMapTO = headersService.getByRefIdAndType(refId, type);
        if (headersMapTO != null) {
            return getHeadersMap(headersMapTO.list(), null);
        }
        return Map.of();
    }

    private Map<String, String> getHeadersMap(List<Header> list, DocumentContext extraContext) {
        DocumentContext headersContext = JsonPath.parse("{}");
        for (Header header : list) {
            var ctx = ValuePipelineContext.builder().result(headersContext).extraContext(extraContext)
                    .build();
            Object val = ValuePipelineUtils.applyPipelines(header.getValuePipelines(), header.getValue(), ctx);
            headersContext.put("$", header.getName(), val != null ? String.valueOf(val) : "");
        }
        return headersContext.read("$");
    }

    public void setupCookies(Connection connection, CookieListTO to, DocumentContext extraContext) {
        if (to != null) {
            Map<String, String> cookieMap = getCookieMap(to.list(), extraContext);
            connection.cookies(cookieMap);
        }
    }

    public void setupHeaders(Connection connection, HeadersTO to, DocumentContext extraContext) {
        if (to != null) {
            Map<String, String> headersMap = getHeadersMap(to.list(), extraContext);
            connection.headers(headersMap);
        }
    }
}
