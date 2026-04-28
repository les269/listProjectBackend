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
import com.lsb.listProjectBackend.entity.dynamic.spider.ExtractionRule;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderItemSetting;
import com.lsb.listProjectBackend.entity.dynamic.spider.ValuePipeline;
import com.lsb.listProjectBackend.service.common.CookieListService;
import com.lsb.listProjectBackend.service.common.ReplaceValueMapService;
import com.lsb.listProjectBackend.utils.Global;
import com.lsb.listProjectBackend.utils.JsonUtils;
import com.lsb.listProjectBackend.utils.Utils;
import com.lsb.listProjectBackend.utils.Global.ExtractionStepCondition;
import com.lsb.listProjectBackend.utils.Global.SpiderUrlType;

import lombok.extern.slf4j.Slf4j;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Slf4j
@UseDynamic
@Service
public class SpiderServiceImpl implements SpiderService {

    @Autowired
    private SpiderConfigService spiderConfigService;

    @Autowired
    private SpiderItemService spiderItemService;

    @Autowired
    private CookieListService cookieListService;

    @Autowired
    private ReplaceValueMapService replaceValueMapService;

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
        return result.json();
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

    private String runSpiderItemByUrl(List<SpiderItemTO> itemList, String url)
            throws Exception {
        DocumentContext result = JsonPath.parse("{}");
        List<String> spiderItemIdsForCookieRef = itemList.stream().filter(
                x -> x.getItemSetting().isUseCookie()).map(SpiderItemTO::getSpiderItemId).distinct().toList();
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
                    cookie = cookieList.getCookieMap();
                }
            }
            Connection connection = getConnection(currentUrl);
            if (cookie != null) {
                connection.cookies(cookie);
            }

            useExtraction(connection, setting, result);
        }
        return result.json();
    }

    private String runSpiderItemByPrimeKeyList(List<SpiderItemTO> itemList, List<String> primeKeyList)
            throws Exception {
        DocumentContext result = JsonPath.parse("{}");

        List<String> spiderItemIdsForCookieRef = itemList.stream().filter(
                x -> x.getItemSetting().isUseCookie()).map(SpiderItemTO::getSpiderItemId).distinct().toList();
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
                    cookie = cookieList.getCookieMap();
                }
            }
            Connection connection = getConnection(currentUrl);
            if (cookie != null) {
                connection.cookies(cookie);
            }

            useExtraction(connection, setting, result);
        }
        return result.json();
    }

    public void useExtraction(Connection connection, SpiderItemSetting setting, DocumentContext result)
            throws IOException {
        switch (setting.getMode()) {
            case Global.ExtractionRuleMode.SELECT:
                Document doc = connection.get();
                useCssSelect(doc.html(), setting.getExtractionRuleList(), result);
                break;
            case Global.ExtractionRuleMode.JSON_PATH:
                String jsonText = connection
                        .ignoreContentType(true)
                        .execute() // 執行請求
                        .body();
                useJsonPath(jsonText, setting.getExtractionRuleList(), result);
                break;
        }
    }

    public void useCssSelect(String html, List<ExtractionRule> extractionRules, DocumentContext result) {
        try {
            Document doc = Jsoup.parse(html);
            List<ReplaceValueMapTO> allReplaceValueMapList = fetchReplaceValueMaps(extractionRules);
            for (ExtractionRule extractionRule : sortedRules(extractionRules)) {
                if (!checkCondition(extractionRule, result)) {
                    continue;
                }
                Elements elements = doc.select(extractionRule.getSelector());
                Object extractedValue = null;
                for (final var pipeLine : enabledPipelines(extractionRule)) {
                    extractedValue = applyPipeline(pipeLine, extractedValue, result, allReplaceValueMapList, elements);
                }
                JsonUtils.putValue(result, extractionRule.getKey(), extractedValue);
            }
        } catch (Exception e) {
            log.error("css select error:", e);
            throw new LsbException(e.getMessage(), e);
        }
    }

    public void useJsonPath(String json, List<ExtractionRule> extractionRules, DocumentContext result) {
        List<ReplaceValueMapTO> allReplaceValueMapList = fetchReplaceValueMaps(extractionRules);
        for (ExtractionRule extractionRule : sortedRules(extractionRules)) {
            if (!checkCondition(extractionRule, result)) {
                continue;
            }
            Object extractedValue = JsonPath.read(json, extractionRule.getJsonPath());
            for (final var pipeLine : enabledPipelines(extractionRule)) {
                extractedValue = applyPipeline(pipeLine, extractedValue, result, allReplaceValueMapList, null);
            }
            JsonUtils.putValue(result, extractionRule.getKey(), extractedValue);
        }
    }

    private List<ReplaceValueMapTO> fetchReplaceValueMaps(List<ExtractionRule> extractionRules) {
        List<String> names = extractionRules.stream()
                .flatMap(rule -> rule.getPipelines().stream())
                .filter(x -> x.isEnabled()
                        && Global.ValuePipelineType.USE_REPLACE_VALUE_MAP.equals(x.getType()))
                .map(x -> x.getUseReplaceValueMap())
                .filter(Utils::isNotBlank)
                .distinct()
                .toList();
        return replaceValueMapService.getAllByNameList(names);
    }

    private List<ExtractionRule> sortedRules(List<ExtractionRule> extractionRules) {
        return extractionRules.stream()
                .sorted((a, b) -> a.getSeq() > b.getSeq() ? 1 : -1)
                .toList();
    }

    private List<ValuePipeline> enabledPipelines(ExtractionRule extractionRule) {
        return extractionRule.getPipelines().stream()
                .filter(x -> x.isEnabled())
                .sorted((a, b) -> a.getSeq() > b.getSeq() ? 1 : -1)
                .toList();
    }

    private Object applyPipeline(ValuePipeline pipeLine, Object extractedValue, DocumentContext result,
            List<ReplaceValueMapTO> allReplaceValueMapList, Elements elements) {
        final var type = pipeLine.getType();
        switch (type) {
            case EXTRACT_ATTR:
                var attributeName = pipeLine.getAttributeName();
                if (elements != null && !elements.isEmpty() && Utils.isNotBlank(attributeName)) {
                    extractedValue = elements.stream().map(x -> x.attr(attributeName)).toList();
                }
                break;
            case EXTRACT_OWN_TEXT:
                if (elements != null && !elements.isEmpty()) {
                    extractedValue = elements.stream().map(Element::ownText).toList();
                }
                break;
            case COMBINE_TO_STRING:
                var combineToString = pipeLine.getCombineToString();
                if (extractedValue instanceof List && Utils.isNotBlank(combineToString)) {
                    extractedValue = JsonUtils.replaceValueByJsonPath(combineToString, extractedValue);
                }
                break;
            case COMBINE_BY_KEY:
                var combineByKey = pipeLine.getCombineByKey();
                if (Utils.isNotBlank(combineByKey)) {
                    extractedValue = JsonUtils.replaceValueByJsonPath(combineByKey, result.json());
                }
                break;
            case CONVERT_TO_ARRAY:
                extractedValue = List.of(extractedValue);
                break;
            case FIRST_VALUE:
                if (extractedValue instanceof List) {
                    var list = (List<?>) extractedValue;
                    extractedValue = list.isEmpty() ? "" : list.get(0);
                }
                break;
            case REPLACE_REGULAR:
                var pattern = pipeLine.getPattern();
                var replacement = pipeLine.getReplacement();
                if (Utils.isNotBlank(pattern) && Utils.isNotBlank(replacement)) {
                    if (extractedValue instanceof List) {
                        extractedValue = ((List<?>) extractedValue).stream()
                                .map(x -> String.valueOf(x).replaceAll(pattern, replacement))
                                .toList();
                    } else {
                        extractedValue = String.valueOf(extractedValue).replaceAll(pattern, replacement);
                    }
                }
                break;
            case SPLIT_TEXT:
                var separator = pipeLine.getSeparator();
                if (Utils.isNotBlank(separator)) {
                    if (extractedValue instanceof List) {
                        extractedValue = ((List<?>) extractedValue).stream()
                                .flatMap(x -> Arrays.stream(String.valueOf(x).split(separator)))
                                .map(String::trim)
                                .filter(Utils::isNotBlank)
                                .toList();
                    } else {
                        extractedValue = Arrays.stream(String.valueOf(extractedValue).split(separator))
                                .map(String::trim)
                                .filter(Utils::isNotBlank)
                                .toList();
                    }
                }
                break;
            case USE_REPLACE_VALUE_MAP:
                var replaceValueMapName = pipeLine.getUseReplaceValueMap();
                if (Utils.isNotBlank(replaceValueMapName)) {
                    ReplaceValueMapTO map = allReplaceValueMapList.stream()
                            .filter(x -> replaceValueMapName.equals(x.getName())).findFirst().orElse(null);
                    if (map != null) {
                        if (extractedValue instanceof List) {
                            extractedValue = ((List<?>) extractedValue).stream().map(x -> {
                                String xStr = String.valueOf(x);
                                return map.getMap().getOrDefault(xStr, xStr);
                            }).toList();
                        } else {
                            String xStr = String.valueOf(extractedValue);
                            extractedValue = map.getMap().getOrDefault(xStr, xStr);
                        }
                    }
                }
                break;
        }
        return extractedValue;
    }

    private boolean checkCondition(ExtractionRule extractionRule, DocumentContext result) {
        final var condition = extractionRule.getCondition();
        if (condition == ExtractionStepCondition.ALWAYS) {
            return true;
        }
        final var conditionValue = extractionRule.getConditionValue();
        final var value = result.read(extractionRule.getConditionKey());
        final var isList = value instanceof List;
        return switch (condition) {
            case ALWAYS -> true;
            case IF_KEY_EMPTY -> value == null
                    || (isList ? ((List<?>) value).isEmpty() : Utils.isBlank(String.valueOf(value)));
            case IF_KEY_NOT_EMPTY -> value != null
                    && (isList ? !((List<?>) value).isEmpty() : Utils.isNotBlank(String.valueOf(value)));
            case CONTAINS -> value != null && matchAny(value, isList, s -> s.contains(conditionValue));
            case NOT_CONTAINS -> value == null || !matchAny(value, isList, s -> s.contains(conditionValue));
            case EQUALS -> value != null && matchAny(value, isList, s -> s.equals(conditionValue));
            case NOT_EQUALS -> value == null || !matchAny(value, isList, s -> s.equals(conditionValue));
            case MATCHES -> value != null && matchAny(value, isList, s -> Pattern.matches(conditionValue, s));
            case NOT_MATCHES -> value == null || !matchAny(value, isList, s -> Pattern.matches(conditionValue, s));
            default -> true;
        };
    }

    private boolean matchAny(Object value, boolean isList, Predicate<String> predicate) {
        if (isList) {
            return ((List<?>) value).stream().anyMatch(x -> predicate.test(String.valueOf(x)));
        }
        return predicate.test(String.valueOf(value));
    }

    protected Connection getConnection(String url) {
        return Jsoup.connect(url)
                .header("Accept-Language", "zh-TW,zh;q=0.9,en-US;q=0.8,en;q=0.7,zh-CN;q=0.6,ja;q=0.5")
                .header("Accept", "*/*")
                .header("Content-Type", "text/html; charset=UTF-8");
    }
}
