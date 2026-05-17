package com.lsb.listProjectBackend.service.spider;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.domain.common.CookieListTO;
import com.lsb.listProjectBackend.domain.common.LsbException;
import com.lsb.listProjectBackend.domain.common.ReplaceValueMapTO;
import com.lsb.listProjectBackend.domain.spider.SpiderConfigTO;
import com.lsb.listProjectBackend.domain.spider.SpiderItemTO;
import com.lsb.listProjectBackend.domain.spider.SpiderTestTO;
import com.lsb.listProjectBackend.entity.dynamic.common.Cookie;
import com.lsb.listProjectBackend.entity.dynamic.spider.ExtractionRule;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderItemSetting;
import com.lsb.listProjectBackend.entity.dynamic.spider.ValuePipeline;
import com.lsb.listProjectBackend.entity.dynamic.spider.ValuePipelineContext;
import com.lsb.listProjectBackend.service.common.CookieListService;
import com.lsb.listProjectBackend.service.common.ReplaceValueMapService;
import com.lsb.listProjectBackend.utils.ValuePipelineUtils;
import com.lsb.listProjectBackend.utils.Global;
import com.lsb.listProjectBackend.utils.JsonUtils;
import com.lsb.listProjectBackend.utils.Utils;
import com.lsb.listProjectBackend.utils.Global.SpiderUrlType;

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

@Slf4j
@UseDynamic
@Service
@RequiredArgsConstructor
public class SpiderServiceImpl implements SpiderService {
    private final SpiderConfigService spiderConfigService;
    private final SpiderItemService spiderItemService;
    private final CookieListService cookieListService;
    private final ExtractionRuleService extractionRuleService;
    private final ReplaceValueMapService replaceValueMapService;

    @Override
    public String executeByUrl(String spiderId, String url) throws Exception {
        SpiderConfigTO config = spiderConfigService.getById(spiderId);
        if (config == null) {
            throw new LsbException("該爬蟲配置不存在: " + spiderId, org.springframework.http.HttpStatus.NOT_FOUND);
        }
        List<SpiderItemTO> itemList = spiderItemService.getBySpiderId(spiderId);

        return runSpiderItemByUrl(itemList, url);
    }

    @Override
    public String executeByPrimeKeyList(String spiderId, List<String> primeKeyList) throws Exception {
        SpiderConfigTO config = spiderConfigService.getById(spiderId);
        if (config == null) {
            throw new LsbException("該爬蟲配置不存在: " + spiderId, org.springframework.http.HttpStatus.NOT_FOUND);
        }
        if (Boolean.TRUE.equals(config.isUrlBased())) {
            throw new LsbException("該爬蟲是無法使用主鍵進行爬蟲的: " + spiderId);
        }
        List<SpiderItemTO> itemList = spiderItemService.getBySpiderId(spiderId);

        return runSpiderItemByPrimeKeyList(itemList, primeKeyList);
    }

    @Override
    public String previewExtraction(SpiderItemTO req) {
        DocumentContext result = JsonPath.parse("{}");
        var setting = req.itemSetting();
        var mode = setting.getMode();

        Map<String, String> cookies = getCookieMap(List.of());
        var cookieListTO = cookieListService.getByRefIdAndType(req.spiderItemId(), Global.CookieListMapType.SPIDER);
        if (cookieListTO != null) {
            cookies = getCookieMap(cookieListTO.list());
        }

        switch (mode) {
            case SELECT ->
                useCssSelect(setting.getTestData().getHtml(), cookies, null, setting.getExtractionRuleList(), result);
            case JSON_PATH ->
                useJsonPath(setting.getTestData().getJson(), cookies, null, setting.getExtractionRuleList(), result);
            default -> throw new LsbException("未知的抽取模式: " + mode);
        }
        return result.jsonString();
    }

    @Override
    public String previewByUrl(SpiderTestTO req) throws Exception {
        String url = req.spiderConfig().testData().getUrl();
        return runSpiderItemByUrl(req.spiderItems(), url);
    }

    @Override
    public String previewByPrimeKey(SpiderTestTO req) throws Exception {
        List<String> primeKeyList = req.spiderConfig().testData().getPrimeKeyList();
        return runSpiderItemByPrimeKeyList(req.spiderItems(), primeKeyList);
    }

    private String runSpiderItemByUrl(List<SpiderItemTO> itemList, String url) throws Exception {
        DocumentContext result = JsonPath.parse("{}");
        List<String> spiderItemIdsForCookieRef = itemList.stream()
                .map(SpiderItemTO::spiderItemId).distinct().toList();
        Map<String, CookieListTO> cookieMap = cookieListService.getMapByRefIdsAndType(spiderItemIdsForCookieRef,
                Global.CookieListMapType.SPIDER);
        boolean firstConnection = true;
        for (SpiderItemTO item : itemList) {
            String currentUrl = "";
            SpiderItemSetting setting = item.itemSetting();
            // 判斷是否為第一次連線且設定為使用URL時要跳過的參數SkipWhenUsingUrl
            if (firstConnection && setting.isSkipWhenUsingUrl()) {
                continue;
            }
            // 非第一次連線但設定為BY_PRIME_KEY時跳過
            if (!firstConnection && SpiderUrlType.BY_PRIME_KEY.equals(setting.getUrlType())) {
                continue;
            }
            if (firstConnection) {
                currentUrl = url;
                firstConnection = false;
            } else {
                currentUrl = JsonUtils.replaceValueByJsonPath(setting.getUrl(), result.json());
            }
            Map<String, String> cookie = null;
            if (cookieMap.containsKey(item.spiderItemId())) {
                CookieListTO cookieList = cookieMap.get(item.spiderItemId());
                if (cookieList != null) {
                    cookie = Utils.getCookieMap(cookieList.list());
                }
            }
            Connection connection = getConnection(currentUrl);
            if (cookie != null) {
                connection.cookies(cookie);
            }

            useExtraction(connection, setting, result);
        }
        return result.read("$");
    }

    private String runSpiderItemByPrimeKeyList(List<SpiderItemTO> itemList, List<String> primeKeyList)
            throws Exception {
        DocumentContext result = JsonPath.parse("{}");

        List<String> spiderItemIdsForCookieRef = itemList.stream()
                .map(SpiderItemTO::spiderItemId).distinct().toList();
        Map<String, CookieListTO> cookieMap = cookieListService.getMapByRefIdsAndType(spiderItemIdsForCookieRef,
                Global.CookieListMapType.SPIDER);
        for (SpiderItemTO item : itemList) {
            String currentUrl = "";
            SpiderItemSetting setting = item.itemSetting();
            // 判斷是否為第一次連線且設定為使用URL時要跳過的參數SkipWhenUsingUrl
            if (SpiderUrlType.BY_PRIME_KEY.equals(setting.getUrlType())) {
                currentUrl = JsonUtils.replaceValueByJsonPath(setting.getUrl(), primeKeyList);
            } else if (SpiderUrlType.BY_PARAMS.equals(setting.getUrlType())) {
                currentUrl = JsonUtils.replaceValueByJsonPath(setting.getUrl(), result.json());
            }
            if (Utils.isBlank(currentUrl)) {
                continue;
            }
            Map<String, String> cookie = null;
            if (cookieMap.containsKey(item.spiderItemId())) {
                CookieListTO cookieList = cookieMap.get(item.spiderItemId());
                if (cookieList != null) {
                    cookie = Utils.getCookieMap(cookieList.list());
                }
            }
            Connection connection = getConnection(currentUrl);
            if (cookie != null) {
                connection.cookies(cookie);
            }

            useExtraction(connection, setting, result);
        }
        return result.read("$");
    }

    public void useExtraction(Connection connection, SpiderItemSetting setting, DocumentContext result)
            throws IOException {
        Document doc;
        Request req;
        switch (setting.getMode()) {
            case Global.ExtractionRuleMode.SELECT:
                doc = connection.get();
                req = connection.request();
                useCssSelect(doc.html(), req.cookies(), req.headers(), setting.getExtractionRuleList(), result);
                break;
            case Global.ExtractionRuleMode.JSON_PATH:
                connection.ignoreContentType(true).get();
                doc = connection.ignoreContentType(true).get();
                req = connection.request();
                useJsonPath(doc.text(), req.cookies(), req.headers(), setting.getExtractionRuleList(), result);
                break;
        }
    }

    public void useCssSelect(String html, Map<String, String> cookies, Map<String, String> headers,
            List<ExtractionRule> extractionRules,
            DocumentContext result) {
        try {
            Document doc = Jsoup.parse(html);
            var allReplaceValueMapList = fetchReplaceValueMaps(extractionRuleService.allPipelines(extractionRules));
            for (ExtractionRule extractionRule : extractionRuleService.sortedRules(extractionRules)) {
                if (!extractionRuleService.checkCondition(extractionRule, result)) {
                    continue;
                }
                var pipelines = extractionRule.getPipelines();
                Elements elements = Utils.isNotBlank(extractionRule.getSelector())
                        ? doc.select(extractionRule.getSelector())
                        : null;
                var context = ValuePipelineContext.builder()
                        .replaceValueMapList(allReplaceValueMapList)
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
        var allReplaceValueMapList = fetchReplaceValueMaps(extractionRuleService.allPipelines(extractionRules));
        for (ExtractionRule extractionRule : extractionRuleService.sortedRules(extractionRules)) {
            if (!extractionRuleService.checkCondition(extractionRule, result)) {
                continue;
            }
            var pipelines = extractionRule.getPipelines();
            Object pipelineValue = JsonPath.read(json, extractionRule.getJsonPath());
            log.info("pipeline value before pipeline: {}", pipelineValue);
            var context = ValuePipelineContext.builder()
                    .replaceValueMapList(allReplaceValueMapList)
                    .result(result)
                    .cookies(cookies != null ? cookies : new HashMap<>())
                    .headers(headers != null ? headers : new HashMap<>())
                    .build();
            pipelineValue = ValuePipelineUtils.applyPipelines(pipelines, pipelineValue, context);
            JsonUtils.putValue(result, extractionRule.getKey(),
                    ValuePipelineUtils.normalizeExtractedValue(pipelineValue));
        }
    }

    protected Connection getConnection(String url) {
        return Jsoup.connect(url).header("Accept-Language", "zh-TW,zh;q=0.9,en-US;q=0.8,en;q=0.7,zh-CN;q=0.6,ja;q=0.5")
                .header("Accept", "*/*").header("Content-Type", "text/html; charset=UTF-8");
    }

    private List<ReplaceValueMapTO> fetchReplaceValueMaps(List<ValuePipeline> pipelines) {
        List<String> names = ValuePipelineUtils.getReplaceValueNameList(pipelines);
        return replaceValueMapService.getAllByNameList(names);
    }

    private Map<String, String> getCookieMap(List<Cookie> list) {
        DocumentContext cookiesContext = JsonPath.parse("{}");
        return list.stream().collect(HashMap::new, (map, cookie) -> {
            var replaceValueMap = fetchReplaceValueMaps(cookie.getValuePipelines());
            var ctx = ValuePipelineContext.builder().result(cookiesContext).replaceValueMapList(replaceValueMap)
                    .build();
            Object val = ValuePipelineUtils.applyPipelines(cookie.getValuePipelines(), cookie.getValue(), ctx);
            map.put(cookie.getName(), val != null ? String.valueOf(val) : "");
        }, HashMap::putAll);
    }
}
