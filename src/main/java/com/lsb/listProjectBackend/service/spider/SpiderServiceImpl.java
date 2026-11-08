package com.lsb.listProjectBackend.service.spider;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.domain.common.LsbException;
import com.lsb.listProjectBackend.domain.spider.SpiderConfigTO;
import com.lsb.listProjectBackend.domain.spider.SpiderItemTO;
import com.lsb.listProjectBackend.domain.spider.SpiderPaginationDataTO;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderItemSetting;
import com.lsb.listProjectBackend.service.common.CookieListService;
import com.lsb.listProjectBackend.service.common.HeadersService;
import com.lsb.listProjectBackend.utils.Global;
import com.lsb.listProjectBackend.utils.JsonUtils;
import com.lsb.listProjectBackend.utils.Utils;
import com.lsb.listProjectBackend.utils.Global.SpiderUrlType;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@UseDynamic
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

        Map<String, String> headers = spiderExecutionService.getHeadersMapByRefId(req.spiderItemId(), Global.HeadersMapType.SPIDER);
        Map<String, String> cookies = spiderExecutionService.getCookieMapByRefId(req.spiderItemId(), Global.CookieListMapType.SPIDER);

        switch (mode) {
            case SELECT ->
                spiderExecutionService.useCssSelect(setting.getTestData().getHtml(), cookies, headers, setting.getExtractionRuleList(),
                        result);
            case JSON_PATH ->
                spiderExecutionService.useJsonPath(setting.getTestData().getJson(), cookies, headers, setting.getExtractionRuleList(), result);
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

    private String runSpiderItemByUrl(List<SpiderItemTO> itemList, String url, Long requestIntervalMs) throws Exception {
        DocumentContext result = JsonPath.parse("{}");
        boolean firstConnection = true;
        boolean hasRequested = false;
        var itemIds = itemList.stream().map(SpiderItemTO::spiderItemId).distinct().toList();
        var cookieMap = cookieListService.getMapByRefIdsAndType(itemIds,
                Global.CookieListMapType.SPIDER);
        var headersMap = headersService.getMapByRefIdsAndType(itemIds, Global.HeadersMapType.SPIDER);
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
            spiderExecutionService.executeRequest(currentUrl, setting.getMode(), setting.getExtractionRuleList(),
                    headersMap.get(item.spiderItemId()), cookieMap.get(item.spiderItemId()), result,
                    hasRequested ? requestIntervalMs : null);
            hasRequested = true;
        }
        return result.jsonString();
    }

    private String runSpiderItemByPrimeKeyList(List<SpiderItemTO> itemList, List<String> primeKeyList,
            Long requestIntervalMs)
            throws Exception {
        DocumentContext result = JsonPath.parse("{}");
        boolean hasRequested = false;

        var itemIds = itemList.stream().map(SpiderItemTO::spiderItemId).distinct().toList();
        var cookieMap = cookieListService.getMapByRefIdsAndType(itemIds,
                Global.CookieListMapType.SPIDER);
        var headersMap = headersService.getMapByRefIdsAndType(itemIds, Global.HeadersMapType.SPIDER);
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
            spiderExecutionService.executeRequest(currentUrl, setting.getMode(), setting.getExtractionRuleList(),
                    headersMap.get(item.spiderItemId()), cookieMap.get(item.spiderItemId()), result,
                    hasRequested ? requestIntervalMs : null);
            hasRequested = true;
        }
        return result.jsonString();
    }

}
