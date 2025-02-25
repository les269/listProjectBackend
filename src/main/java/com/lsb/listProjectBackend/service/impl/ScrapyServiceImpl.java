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
import java.util.stream.Stream;

@Slf4j
@Service
public class ScrapyServiceImpl extends ScrapyBase implements ScrapyService {
    @Autowired
    private ScrapyConfigRepository scrapyConfigRepository;

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
                var handleJson = handlePreJson(data, json);
                //當是轉址時要確保url不為空
                if (Global.ScrapyPageType.redirect == data.getScrapyPageType() && Utils.isNotBlank(data.getUrl())) {
                    //替換url的參數
                    String url = Utils.replaceValue(data.getUrl(), handleJson);
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
                    String url = Utils.replaceValue(data.getUrl(), handleJson);
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
        boolean redirect = false;
        String redirectUrl = "";
        for (var data : scrapyDataList) {
            var _url = url;
            if (redirect) {
                _url = redirectUrl;
                redirect = false;
                redirectUrl = "";
            }
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
                if (result.containsKey("__redirect") && Utils.isNotBlank(result.get("__redirect").toString())) {
                    redirect = true;
                    redirectUrl = result.get("__redirect").toString();
                    result.remove("__redirect");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }



    private List<String> handlePreJson(ScrapyData scrapyData, List<String> json) {
        if (Utils.isNotBlank(scrapyData.getReplaceRegular())) {
            return json.stream().map(x -> x.replaceAll(scrapyData.getReplaceRegular(), scrapyData.getReplaceRegularTo())).toList();
        }
        return json;
    }
}
