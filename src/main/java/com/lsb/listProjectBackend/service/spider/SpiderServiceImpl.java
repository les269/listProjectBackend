package com.lsb.listProjectBackend.service.spider;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.domain.common.LsbException;
import com.lsb.listProjectBackend.domain.spider.SpiderConfigTO;
import com.lsb.listProjectBackend.domain.spider.SpiderItemTO;
import com.lsb.listProjectBackend.domain.spider.SpiderPaginationDataTO;
import com.lsb.listProjectBackend.domain.spider.SpiderReqTO;
import com.lsb.listProjectBackend.domain.common.CookieListTO;
import com.lsb.listProjectBackend.domain.common.HeadersTO;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderItemSetting;
import com.lsb.listProjectBackend.service.common.CookieListService;
import com.lsb.listProjectBackend.service.common.HeadersService;
import com.lsb.listProjectBackend.utils.Global;
import com.lsb.listProjectBackend.utils.JsonUtils;
import com.lsb.listProjectBackend.utils.Utils;
import com.lsb.listProjectBackend.utils.Global.SpiderUrlType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@UseDynamic
@Slf4j
@Service
@RequiredArgsConstructor
public class SpiderServiceImpl implements SpiderService {
    private final SpiderConfigService spiderConfigService;
    private final SpiderItemService spiderItemService;
    private final CookieListService cookieListService;
    private final SpiderExecutionService spiderExecutionService;
    private final HeadersService headersService;
    private final SpiderPaginationExecutionService paginationExecutionService;

    @Override
    public List<String> executeBatch(String spiderId, List<SpiderReqTO> requests) throws Exception {
        if (requests.isEmpty()) {
            return List.of();
        }
        SpiderConfigTO config = spiderConfigService.getById(spiderId);
        if (config == null) {
            throw new LsbException("該爬蟲配置不存在: " + spiderId, org.springframework.http.HttpStatus.NOT_FOUND);
        }
        List<SpiderItemTO> items = spiderItemService.getBySpiderId(spiderId);
        var itemIds = items.stream().map(SpiderItemTO::spiderItemId).distinct().toList();
        var cookies = cookieListService.getMapByRefIdsAndType(itemIds, Global.CookieListMapType.SPIDER);
        var headers = headersService.getMapByRefIdsAndType(itemIds, Global.HeadersMapType.SPIDER);
        var results = new ArrayList<String>();
        for (SpiderReqTO request : requests) {
            if (!results.isEmpty()) {
                spiderExecutionService.waitForRequestInterval(config.requestIntervalMs());
            }
            if (Boolean.TRUE.equals(config.isUrlBased()) && Utils.isNotBlank(request.url())) {
                results.add(runSpiderItemByUrl(items, request.url(), config.requestIntervalMs(), cookies, headers));
            } else {
                results.add(runSpiderItemByPrimeKeyList(items, request.primeKeyList(), config.requestIntervalMs(),
                        cookies, headers));
            }
        }
        return results;
    }

    @Override
    public List<SpiderPaginationDataTO> executePagination(String spiderPaginationId) throws Exception {
        return executePagination(spiderPaginationId, false);
    }

    @Override
    public List<SpiderPaginationDataTO> executePagination(String spiderPaginationId, boolean force) throws Exception {
        return paginationExecutionService.execute(spiderPaginationId, force);
    }

    @Override
    public String executeByUrl(String spiderId, String url) throws Exception {
        SpiderConfigTO config = spiderConfigService.getById(spiderId);
        if (config == null) {
            throw new LsbException("該爬蟲配置不存在: " + spiderId, org.springframework.http.HttpStatus.NOT_FOUND);
        }
        List<SpiderItemTO> itemList = spiderItemService.getBySpiderId(spiderId);

        return runSpiderItemByUrl(itemList, url, config.requestIntervalMs());
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

        return runSpiderItemByPrimeKeyList(itemList, primeKeyList, config.requestIntervalMs());
    }

    @Override
    public String previewExtraction(SpiderItemTO req) {
        DocumentContext result = JsonPath.parse("{}");
        var setting = req.itemSetting();
        var mode = setting.getMode();

        Map<String, String> headers = spiderExecutionService.getHeadersMapByRefId(req.spiderItemId(),
                Global.HeadersMapType.SPIDER);
        Map<String, String> cookies = spiderExecutionService.getCookieMapByRefId(req.spiderItemId(),
                Global.CookieListMapType.SPIDER);

        switch (mode) {
            case SELECT ->
                spiderExecutionService.useCssSelect(setting.getTestData().getHtml(), cookies, headers,
                        setting.getExtractionRuleList(),
                        result);
            case JSON_PATH ->
                spiderExecutionService.useJsonPath(setting.getTestData().getJson(), cookies, headers,
                        setting.getExtractionRuleList(), result);
            default -> throw new LsbException("未知的抽取模式: " + mode);
        }
        return result.jsonString();
    }

    @Override
    public String previewExtractionWithFile(MultipartFile file, SpiderItemTO setting) throws Exception {
        // 這裡可以根據需要處理上傳的文件，例如解析文件內容
        byte[] fileContent = file.getBytes();
        // 可以將文件內容與設定一起傳遞給相應的處理方法
        String fileContentString = new String(fileContent);
        var mode = setting.itemSetting().getMode();
        var testData = setting.itemSetting().getTestData();
        switch (mode) {
            case SELECT ->
                testData.setHtml(fileContentString);
            case JSON_PATH ->
                testData.setJson(fileContentString);
            default -> throw new LsbException("未知的抽取模式: " + mode);
        }
        return previewExtraction(setting);
    }

    private String runSpiderItemByUrl(List<SpiderItemTO> itemList, String url, Long requestIntervalMs)
            throws Exception {
        var itemIds = itemList.stream().map(SpiderItemTO::spiderItemId).distinct().toList();
        var cookieMap = cookieListService.getMapByRefIdsAndType(itemIds,
                Global.CookieListMapType.SPIDER);
        var headersMap = headersService.getMapByRefIdsAndType(itemIds, Global.HeadersMapType.SPIDER);
        return runSpiderItemByUrl(itemList, url, requestIntervalMs, cookieMap, headersMap);
    }

    private String runSpiderItemByUrl(List<SpiderItemTO> itemList, String url, Long requestIntervalMs,
            Map<String, CookieListTO> cookieMap, Map<String, HeadersTO> headersMap) throws Exception {
        DocumentContext result = JsonPath.parse("{}");
        boolean firstConnection = true;
        boolean hasRequested = false;
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
            executeItemSafely(item, currentUrl, headersMap.get(item.spiderItemId()),
                    cookieMap.get(item.spiderItemId()), result, hasRequested ? requestIntervalMs : null, null);
            hasRequested = hasRequested || Utils.isNotBlank(currentUrl);
        }
        return result.jsonString();
    }

    private String runSpiderItemByPrimeKeyList(List<SpiderItemTO> itemList, List<String> primeKeyList,
            Long requestIntervalMs)
            throws Exception {
        var itemIds = itemList.stream().map(SpiderItemTO::spiderItemId).distinct().toList();
        var cookieMap = cookieListService.getMapByRefIdsAndType(itemIds,
                Global.CookieListMapType.SPIDER);
        var headersMap = headersService.getMapByRefIdsAndType(itemIds, Global.HeadersMapType.SPIDER);
        return runSpiderItemByPrimeKeyList(itemList, primeKeyList, requestIntervalMs, cookieMap, headersMap);
    }

    private String runSpiderItemByPrimeKeyList(List<SpiderItemTO> itemList, List<String> primeKeyList,
            Long requestIntervalMs, Map<String, CookieListTO> cookieMap, Map<String, HeadersTO> headersMap)
            throws Exception {
        DocumentContext result = JsonPath.parse("{}");
        boolean hasRequested = false;
        for (SpiderItemTO item : itemList) {
            String currentUrl = "";
            SpiderItemSetting setting = item.itemSetting();
            // 判斷是否為第一次連線且設定為使用URL時要跳過的參數SkipWhenUsingUrl
            if (SpiderUrlType.BY_PRIME_KEY.equals(setting.getUrlType())) {
                currentUrl = JsonUtils.replaceValueByJsonPath(setting.getUrl(), primeKeyList);
            } else if (SpiderUrlType.BY_PARAMS.equals(setting.getUrlType())) {
                currentUrl = JsonUtils.replaceValueByJsonPath(setting.getUrl(), result.json());
            }
            executeItemSafely(item, currentUrl, headersMap.get(item.spiderItemId()),
                    cookieMap.get(item.spiderItemId()), result, hasRequested ? requestIntervalMs : null, primeKeyList);
            hasRequested = hasRequested || Utils.isNotBlank(currentUrl);
        }
        return result.jsonString();
    }

    /** 單一步驟失敗時保留已擷取的資料，繼續後續步驟與批次中的其他筆資料。 */
    private void executeItemSafely(SpiderItemTO item, String url, HeadersTO headers, CookieListTO cookies,
            DocumentContext result, Long requestIntervalMs, List<String> initialKeys) throws InterruptedException {
        var setting = item.itemSetting();
        try {
            if (initialKeys == null) {
                spiderExecutionService.executeRequest(url, setting.getMode(), setting.getExtractionRuleList(),
                        headers, cookies, result, requestIntervalMs);
            } else {
                spiderExecutionService.executeRequest(url, setting.getMode(), setting.getExtractionRuleList(),
                        headers, cookies, result, requestIntervalMs, initialKeys);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } catch (java.io.IOException | RuntimeException e) {
            log.warn("Spider item failed; continuing with collected data. itemId={}, url={}",
                    item.spiderItemId(), url, e);
        }
    }

}
