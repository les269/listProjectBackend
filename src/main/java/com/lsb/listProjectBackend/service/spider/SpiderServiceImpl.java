package com.lsb.listProjectBackend.service.spider;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.domain.common.CookieListTO;
import com.lsb.listProjectBackend.domain.common.LsbException;
import com.lsb.listProjectBackend.domain.spider.SpiderConfigTO;
import com.lsb.listProjectBackend.domain.spider.SpiderItemTO;
import com.lsb.listProjectBackend.domain.spider.SpiderTestTO;
import com.lsb.listProjectBackend.entity.dynamic.spider.ExtractionRule;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderItemSetting;
import com.lsb.listProjectBackend.entity.dynamic.spider.ValuePipelineContext;
import com.lsb.listProjectBackend.service.common.CookieListService;
import com.lsb.listProjectBackend.utils.Global;
import com.lsb.listProjectBackend.utils.JsonUtils;
import com.lsb.listProjectBackend.utils.Utils;
import com.lsb.listProjectBackend.utils.Global.SpiderUrlType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
    private final ValuePipelineService valuePipelineService;
    private final ExtractionRuleService extractionRuleService;

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
        if (Boolean.TRUE.equals(config.getIsUrlBased())) {
            throw new LsbException("該爬蟲是無法使用主鍵進行爬蟲的: " + spiderId);
        }
        List<SpiderItemTO> itemList = spiderItemService.getBySpiderId(spiderId);

        return runSpiderItemByPrimeKeyList(itemList, primeKeyList);
    }

    @Override
    public String previewExtraction(SpiderItemSetting setting) {
        DocumentContext result = JsonPath.parse("{}");
        var mode = setting.getMode();
        switch (mode) {
            case SELECT -> useCssSelect(setting.getTestData().getHtml(), setting.getExtractionRuleList(), result);
            case JSON_PATH -> useJsonPath(setting.getTestData().getJson(), setting.getExtractionRuleList(), result);
            default -> throw new LsbException("未知的抽取模式: " + mode);
        }
        return result.jsonString();
    }

    @Override
    public String previewByUrl(SpiderTestTO req) throws Exception {
        String url = req.getSpiderConfig().getTestData().getUrl();
        return runSpiderItemByUrl(req.getSpiderItems(), url);
    }

    @Override
    public String previewByPrimeKey(SpiderTestTO req) throws Exception {
        List<String> primeKeyList = req.getSpiderConfig().getTestData().getPrimeKeyList();
        return runSpiderItemByPrimeKeyList(req.getSpiderItems(), primeKeyList);
    }

    private String runSpiderItemByUrl(List<SpiderItemTO> itemList, String url) throws Exception {
        DocumentContext result = JsonPath.parse("{}");
        List<String> spiderItemIdsForCookieRef = itemList.stream().filter(x -> x.getItemSetting().isUseCookie())
                .map(SpiderItemTO::getSpiderItemId).distinct().toList();
        Map<String, CookieListTO> cookieMap = cookieListService.getMapByRefIdsAndType(spiderItemIdsForCookieRef,
                Global.CookieListMapType.SPIDER);
        boolean firstConnection = true;
        for (SpiderItemTO item : itemList) {
            String currentUrl = "";
            SpiderItemSetting setting = item.getItemSetting();
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
            if (setting.isUseCookie() && cookieMap.containsKey(item.getSpiderItemId())) {
                CookieListTO cookieList = cookieMap.get(item.getSpiderItemId());
                if (cookieList != null) {
                    cookie = Utils.getCookieMap(cookieList.getList());
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

        List<String> spiderItemIdsForCookieRef = itemList.stream().filter(x -> x.getItemSetting().isUseCookie())
                .map(SpiderItemTO::getSpiderItemId).distinct().toList();
        Map<String, CookieListTO> cookieMap = cookieListService.getMapByRefIdsAndType(spiderItemIdsForCookieRef,
                Global.CookieListMapType.SPIDER);
        for (SpiderItemTO item : itemList) {
            String currentUrl = "";
            SpiderItemSetting setting = item.getItemSetting();
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
            if (setting.isUseCookie() && cookieMap.containsKey(item.getSpiderItemId())) {
                CookieListTO cookieList = cookieMap.get(item.getSpiderItemId());
                if (cookieList != null) {
                    cookie = Utils.getCookieMap(cookieList.getList());
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
        switch (setting.getMode()) {
            case Global.ExtractionRuleMode.SELECT:
                Document doc = connection.get();
                useCssSelect(doc.html(), setting.getExtractionRuleList(), result);
                break;
            case Global.ExtractionRuleMode.JSON_PATH:
                String jsonText = connection.ignoreContentType(true).execute() // 執行請求
                        .body();
                useJsonPath(jsonText, setting.getExtractionRuleList(), result);
                break;
        }
    }

    public void useCssSelect(String html, List<ExtractionRule> extractionRules, DocumentContext result) {
        try {
            Document doc = Jsoup.parse(html);
            var allReplaceValueMapList = valuePipelineService
                    .fetchReplaceValueMaps(extractionRuleService.allPipelines(extractionRules));
            for (ExtractionRule extractionRule : extractionRuleService.sortedRules(extractionRules)) {
                if (!extractionRuleService.checkCondition(extractionRule, result)) {
                    continue;
                }
                var pipelines = extractionRule.getPipelines();
                Elements elements = Utils.isNotBlank(extractionRule.getSelector())
                        ? doc.select(extractionRule.getSelector())
                        : null;
                var context = new ValuePipelineContext(allReplaceValueMapList, result, elements);
                Object pipelineValue = valuePipelineService.applyPipelines(pipelines, null, context);
                if (elements != null && !valuePipelineService.hasEnabledPipelines(pipelines)) {
                    pipelineValue = elements.stream().map(Element::text).toList();
                }
                JsonUtils.putValue(result, extractionRule.getKey(),
                        valuePipelineService.normalizeExtractedValue(pipelineValue));
            }
        } catch (Exception e) {
            log.error("css select error:", e);
            throw new LsbException(e.getMessage(), e);
        }
    }

    public void useJsonPath(String json, List<ExtractionRule> extractionRules, DocumentContext result) {
        var allReplaceValueMapList = valuePipelineService
                .fetchReplaceValueMaps(extractionRuleService.allPipelines(extractionRules));
        for (ExtractionRule extractionRule : extractionRuleService.sortedRules(extractionRules)) {
            if (!extractionRuleService.checkCondition(extractionRule, result)) {
                continue;
            }
            var pipelines = extractionRule.getPipelines();
            Object pipelineValue = JsonPath.read(json, extractionRule.getJsonPath());
            log.info("pipeline value before pipeline: {}", pipelineValue);
            var context = new ValuePipelineContext(allReplaceValueMapList, result, null);
            pipelineValue = valuePipelineService.applyPipelines(pipelines, pipelineValue, context);
            JsonUtils.putValue(result, extractionRule.getKey(),
                    valuePipelineService.normalizeExtractedValue(pipelineValue));
        }
    }


    protected Connection getConnection(String url) {
        return Jsoup.connect(url).header("Accept-Language", "zh-TW,zh;q=0.9,en-US;q=0.8,en;q=0.7,zh-CN;q=0.6,ja;q=0.5")
                .header("Accept", "*/*").header("Content-Type", "text/html; charset=UTF-8");
    }

}
