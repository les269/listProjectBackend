package com.lsb.listProjectBackend.service.impl;

import com.lsb.listProjectBackend.domain.LsbException;
import com.lsb.listProjectBackend.domain.ScrapyConfigTO;
import com.lsb.listProjectBackend.domain.ScrapyReqTO;
import com.lsb.listProjectBackend.entity.*;
import com.lsb.listProjectBackend.mapper.ScrapyConfigMapper;
import com.lsb.listProjectBackend.repository.ReplaceValueMapRepository;
import com.lsb.listProjectBackend.repository.ScrapyConfigRepository;
import com.lsb.listProjectBackend.service.ScrapyService;
import com.lsb.listProjectBackend.utils.Global;
import com.lsb.listProjectBackend.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ScrapyServiceImpl implements ScrapyService {
    @Autowired
    private ScrapyConfigRepository scrapyConfigRepository;
    @Autowired
    private ReplaceValueMapRepository replaceValueMapRepository;

    private final ScrapyConfigMapper scrapyConfigMapper = ScrapyConfigMapper.INSTANCE;

    public List<ScrapyConfigTO> getAllConfig() {
        return scrapyConfigMapper.toDomainList(scrapyConfigRepository.findAll());
    }

    public void updateConfig(ScrapyConfigTO req) {
        ScrapyConfig scrapyConfig = scrapyConfigMapper.toEntity(req);
        scrapyConfigRepository.save(scrapyConfig);
    }

    public void deleteConfig(String name) {
        scrapyConfigRepository.deleteById(name);
    }

    public ScrapyConfigTO getConfig(String name) {
        return scrapyConfigMapper.toDomain(scrapyConfigRepository.findById(name).orElse(null));
    }

    public boolean existConfig(String name) {
        return scrapyConfigRepository.existsById(name);
    }

    public List<String> getAllName() {
        return scrapyConfigRepository.findAll().stream().map(ScrapyConfig::getName).toList();
    }

    public List<ScrapyConfigTO> getByNameList(List<String> nameList) {
        return scrapyConfigMapper.toDomainList(scrapyConfigRepository.findAllById(nameList));
    }

    public Map<String, Object> testParseHtml(String html, ScrapyData data) {
        Map<String, Object> result = new HashMap<>();
        useCssSelect(html, data.getCssSelectList(), result);
        return result;
    }

    public Map<String, Object> scrapyByJson(ScrapyReqTO to) throws Exception {
        ScrapyConfigTO scrapyConfigTO = getConfig(to.getScrapyName());
        if (scrapyConfigTO != null) {
            return doScrapyByJson(to.getJson(), scrapyConfigTO.getData());
        }
        return new HashMap<>();
    }

    public Map<String, Object> doScrapyByJson(List<String> json, List<ScrapyData> scrapyDataList) throws Exception {
        Map<String, Object> result = new HashMap<>();
        boolean redirect = false;
        String redirectUrl = "";
        try {
            for (ScrapyData data : scrapyDataList) {
                Map<String, String> cookies = data.getCookie().stream().
                        collect(Collectors.toMap(Cookie::getName, Cookie::getValue));
                //當是轉址時要確保url不為空
                if (Global.ScrapyPageType.redirect == data.getScrapyPageType() && Utils.isNotBlank(data.getUrl())) {
                    //替換url的參數
                    String url = Utils.replaceValue(data.getUrl(), json);
                    // 當有轉址了就用轉址來query
                    if (redirect && Utils.isNotBlank(redirectUrl)) {
                        url = redirectUrl;
                    }
                    Document document = getConnection(url).cookies(cookies).followRedirects(true).get();
                    useCssSelect(document.html(), data.getCssSelectList(), result);
                    if (result.containsKey("__redirect") && Utils.isNotBlank(result.get("__redirect").toString())) {
                        redirect = true;
                        redirectUrl = result.get("__redirect").toString();
                        result.remove("__redirect");
                    }
                } else if (redirect && Utils.isNotBlank(redirectUrl) && Global.ScrapyPageType.scrapyData == data.getScrapyPageType()) {
                    Document document = getConnection(redirectUrl)
                            .cookies(cookies).get();
                    useCssSelect(document.html(), data.getCssSelectList(), result);
                    redirect = false;
                    redirectUrl = "";
                } else if (Global.ScrapyPageType.scrapyData == data.getScrapyPageType() && Utils.isNotBlank(data.getUrl())) {
                    String url = Utils.replaceValue(data.getUrl(), json);
                    Document document = getConnection(url)
                            .cookies(cookies).get();
                    useCssSelect(document.html(), data.getCssSelectList(), result);
                }
            }
        } catch (Exception e) {
            log.error("scrapy error", e);
        }

        return result;
    }

    public Map<String, Object> scrapyByUrl(ScrapyReqTO to) {
        ScrapyConfigTO scrapyConfigTO = getConfig(to.getScrapyName());
        if (scrapyConfigTO != null) {
            return doScrapyByUrl(to.getUrl(), scrapyConfigTO.getData());
        }
        return new HashMap<>();
    }

    public Map<String, Object> doScrapyByUrl(String url, List<ScrapyData> scrapyDataList) {
        Map<String, Object> result = new HashMap<>();
        scrapyDataList.stream()
                .filter(x -> x.getScrapyPageType() == Global.ScrapyPageType.scrapyData)
                .findFirst()
                .ifPresent(data -> {
                    var _url = url;
                    Map<String, String> cookies = data.getCookie().stream().
                            collect(Collectors.toMap(Cookie::getName, Cookie::getValue));
                    if (Utils.isNotBlank(data.getUrl())) {
                        _url = Utils.replaceValue(data.getUrl(), List.of(url));
                    }
                    try {
                        Document document = getConnection(_url)
                                .cookies(cookies)
                                .postDataCharset("UTF-8")
                                .get();
                        useCssSelect(document.html(), data.getCssSelectList(), result);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        return result;
    }

    public void useCssSelect(String htmlString, List<CssSelect> select, Map<String, Object> result) {
        try {
            Document doc = Jsoup.parse(htmlString);
            for (CssSelect cssSelect : select.stream().sorted((a, b) -> a.getSeq() > b.getSeq() ? 1 : -1).toList()) {
                var value = Utils.replaceValue(cssSelect.getValue(), result);
                List<String> textList = doc.select(value)
                        .stream()
                        .map(x -> {
                            if (Utils.isNotBlank(cssSelect.getAttr())) {
                                return x.attr(cssSelect.getAttr());
                            }
                            if (cssSelect.isOnlyOwn()) {
                                return x.ownText();
                            }
                            return x.text();
                        })
                        .filter(Utils::isNotBlank)
                        .map(x -> {
                            if (Utils.isNotBlank(cssSelect.getReplaceRegular())) {
                                return x.replaceAll(cssSelect.getReplaceRegular(), cssSelect.getReplaceRegularTo());
                            }
                            return x;
                        })
                        .map(String::trim)
                        .toList();
                if (Utils.isNotBlank(cssSelect.getReplaceString()) && !textList.isEmpty()) {
                    String replacedValue = Utils.replaceValue(cssSelect.getReplaceString(), textList);
                    result.put(cssSelect.getKey(), cssSelect.isConvertToArray() ? List.of(replacedValue) : replacedValue);
                    continue;
                }
                // 判斷是否合併已有資料
                mergeResult(result, cssSelect.getKey(), textList, cssSelect.isConvertToArray());
            }
            Map<String, Map<String, Object>> replaceValueMap = new HashMap<>();
            for (CssSelect cssSelect : select) {
                resultReplaceValue(cssSelect, result, replaceValueMap);
            }
        } catch (Exception e) {
            log.error("An error occurred", e);
            throw new LsbException(e.getMessage());
        }
    }

    private void mergeResult(Map<String, Object> result, String key, List<String> newTextList, boolean convertToArray) {
        if (newTextList.isEmpty()) {
            result.put(key, convertToArray ? List.of() : "");
            return;
        }

        if (convertToArray) {
            List<String> mergedList = new ArrayList<>(newTextList);
            if (result.containsKey(key) && result.get(key) instanceof List) {
                mergedList.addAll((List<String>) result.get(key));
            }
            result.put(key, mergedList.stream().distinct().toList());
        } else {
            result.put(key, newTextList.size() == 1 ? newTextList.get(0) : newTextList);
        }
    }

    private void resultReplaceValue(CssSelect cssSelect, Map<String, Object> result, Map<String, Map<String, Object>> replaceValueMap) {
        // 提前返回，如果沒有 replaceValueMapName
        var name = cssSelect.getReplaceValueMapName();
        if (Utils.isBlank(name)) return;
        // 獲取 key
        var key = cssSelect.getKey();

        Map<String, Object> map = replaceValueMap.computeIfAbsent(name, k ->
                replaceValueMapRepository.findById(k)
                        .map(ReplaceValueMap::getMap)
                        .orElseGet(HashMap::new)
        );

        // 處理 result 中的 key，確保其值為 List<String>
        if (result.containsKey(key) && result.get(key) instanceof List<?> list) {
            List<String> replacedList = list.stream()
                    .map(Object::toString) // 確保元素轉為 String
                    .map(s -> map.containsKey(s) && Utils.isNotBlank(map.get(s).toString()) ? map.get(s).toString() : s)
                    .toList();
            result.put(key, replacedList);
        }
    }

    private Connection getConnection(String url) {
        return Jsoup.connect(url)
                .header("Accept-Language", "zh-TW,zh;q=0.9,en-US;q=0.8,en;q=0.7,zh-CN;q=0.6,ja;q=0.5")
                .header("Accept", "*/*")
                .header("Content-Type", "text/html; charset=UTF-8");
    }
}
