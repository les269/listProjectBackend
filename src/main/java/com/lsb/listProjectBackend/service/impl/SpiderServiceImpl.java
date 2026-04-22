package com.lsb.listProjectBackend.service.impl;

import com.jayway.jsonpath.JsonPath;
import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.domain.CookieListTO;
import com.lsb.listProjectBackend.domain.LsbException;
import com.lsb.listProjectBackend.domain.SpiderConfigTO;
import com.lsb.listProjectBackend.domain.SpiderItemTO;
import com.lsb.listProjectBackend.domain.SpiderMappingTO;
import com.lsb.listProjectBackend.entity.dynamic.ExtractionRule;
import com.lsb.listProjectBackend.entity.dynamic.SpiderItemSetting;
import com.lsb.listProjectBackend.entity.dynamic.ValuePipeline;
import com.lsb.listProjectBackend.service.CookieListService;
import com.lsb.listProjectBackend.service.SpiderConfigService;
import com.lsb.listProjectBackend.service.SpiderItemService;
import com.lsb.listProjectBackend.service.SpiderMappingService;
import com.lsb.listProjectBackend.service.SpiderService;
import com.lsb.listProjectBackend.utils.Global;
import com.lsb.listProjectBackend.utils.Utils;
import com.lsb.listProjectBackend.utils.Global.SpiderUrlType;

import lombok.extern.slf4j.Slf4j;

import org.checkerframework.checker.units.qual.A;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    @Override
    public Map<String, Object> executeByUrl(String spiderId, String url) throws Exception {
        SpiderConfigTO config = spiderConfigService.getById(spiderId);
        if (config == null) {
            throw new LsbException("該爬蟲配置不存在: " + spiderId);
        }
        List<SpiderItemTO> itemList = spiderItemService.getBySpiderId(spiderId);

        return runSpiderItemByUrl(itemList, url);
    }

    @Override
    public Map<String, Object> executeByPrimeKeyList(String spiderId, List<String> primeKeyList) throws Exception {
        SpiderConfigTO config = spiderConfigService.getById(spiderId);
        if (config == null) {
            throw new LsbException("該爬蟲配置不存在: " + spiderId);
        }
        if (Boolean.TRUE.equals(config.getIsUrlBased())) {
            throw new LsbException("該爬蟲是無法使用主鍵進行爬蟲的: " + spiderId);
        }
        List<SpiderItemTO> itemList = spiderItemService.getBySpiderId(spiderId);

        return runSpiderItemByPrimeKeyList(itemList, primeKeyList);
    }

    private Map<String, Object> runSpiderItemByUrl(List<SpiderItemTO> itemList, String url)
            throws Exception {
        Map<String, Object> result = new HashMap<>();
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
                currentUrl = Utils.replaceValueByJsonPath(setting.getUrl(), result);
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
        return result;
    }

    private Map<String, Object> runSpiderItemByPrimeKeyList(List<SpiderItemTO> itemList, List<String> primeKeyList)
            throws Exception {
        Map<String, Object> result = new HashMap<>();
        List<String> spiderItemIdsForCookieRef = itemList.stream().filter(
                x -> x.getItemSetting().isUseCookie()).map(SpiderItemTO::getSpiderItemId).distinct().toList();
        Map<String, CookieListTO> cookieMap = cookieListService.getMapByRefIdsAndType(spiderItemIdsForCookieRef,
                Global.CookieListMapType.SPIDER);

        return result;
    }

    public void useExtraction(Connection connection, SpiderItemSetting setting, Map<String, Object> result)
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

    public void useCssSelect(String html, List<ExtractionRule> select, Map<String, Object> result) {

    }

    public void useJsonPath(String json, List<ExtractionRule> jsonPathList, Map<String, Object> result) {

    }

    protected Connection getConnection(String url) {
        return Jsoup.connect(url)
                .header("Accept-Language", "zh-TW,zh;q=0.9,en-US;q=0.8,en;q=0.7,zh-CN;q=0.6,ja;q=0.5")
                .header("Accept", "*/*")
                .header("Content-Type", "text/html; charset=UTF-8");
    }
}
