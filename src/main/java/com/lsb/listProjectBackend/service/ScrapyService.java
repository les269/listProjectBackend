package com.lsb.listProjectBackend.service;

import com.lsb.listProjectBackend.domain.LsbException;
import com.lsb.listProjectBackend.domain.ScrapyConfigTO;
import com.lsb.listProjectBackend.domain.ScrapyTestTO;
import com.lsb.listProjectBackend.entity.Cookie;
import com.lsb.listProjectBackend.entity.CssSelect;
import com.lsb.listProjectBackend.entity.ScrapyConfig;
import com.lsb.listProjectBackend.entity.ScrapyData;
import com.lsb.listProjectBackend.mapper.ScrapyConfigMapper;
import com.lsb.listProjectBackend.repository.ScrapyConfigRepository;
import com.lsb.listProjectBackend.utils.Global;
import com.lsb.listProjectBackend.utils.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Service
public class ScrapyService {
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
    public List<String> getAllName(){
        return scrapyConfigRepository.findAll().stream().map(ScrapyConfig::getName).toList();
    }

    public Map<String, Object> testParseHtml(String html, ScrapyData data) {
        Map<String, Object> result = new HashMap<>();
        useCssSelect(html, data.getCssSelectList(), result);
        return result;
    }

    public Map<String, Object> testJson(ScrapyTestTO to) throws Exception {
        Map<String, Object> result = new HashMap<>();
        boolean redirect = false;
        String redirectUrl = "";

        for (ScrapyData data : to.getScrapyDataList()) {
            Map<String, String> cookies = data.getCookie().stream().
                    collect(Collectors.toMap(Cookie::getName, Cookie::getValue));
            //當是轉址時要確保url不為空
            if (Global.ScrapyPageType.redirect == data.getScrapyPageType() && Utils.isNotBlank(data.getUrl())) {
                //替換url的參數
                String url = Utils.replaceValue(data.getUrl(), to.getJson());
                // 當有轉址了就用轉址來query
                if (redirect && Utils.isNotBlank(redirectUrl)) {
                    url = redirectUrl;
                }
                Document document = Jsoup.connect(url).cookies(cookies).get();
                useCssSelect(document.html(), data.getCssSelectList(), result);
                if (result.containsKey("__redirect") && Utils.isNotBlank(result.get("__redirect").toString())) {
                    redirect = true;
                    redirectUrl = result.get("__redirect").toString();
                    result.remove("__redirect");
                }
            } else if (redirect && Utils.isNotBlank(redirectUrl) && Global.ScrapyPageType.scrapyData == data.getScrapyPageType()) {
                Document document = Jsoup.connect(redirectUrl).cookies(cookies).get();
                useCssSelect(document.html(), data.getCssSelectList(), result);
                redirect = false;
                redirectUrl = "";
            }
        }
        return result;
    }

    public Map<String, Object> testUrl(ScrapyTestTO to) throws Exception {
        Map<String, Object> result = new HashMap<>();
        to.getScrapyDataList().stream()
                .filter(x -> x.getScrapyPageType() == Global.ScrapyPageType.scrapyData)
                .findFirst()
                .ifPresent(data -> {
                    Map<String, String> cookies = data.getCookie().stream().
                            collect(Collectors.toMap(Cookie::getName, Cookie::getValue));
                    try {
                        Document document = Jsoup.connect(to.getUrl()).cookies(cookies).get();
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
            for (CssSelect cssSelect : select) {
                List<String> textList = doc.select(cssSelect.getValue())
                        .stream()
                        .map(x -> {
                            if (Utils.isNotBlank(cssSelect.getAttr())) {
                                return x.attr(cssSelect.getAttr());
                            }
                            return x.text();
                        })
                        .filter(Utils::isNotBlank)
                        .map(String::trim)
                        .toList();
                if (Utils.isNotBlank(cssSelect.getReplaceString())) {
                    String rv = Utils.replaceValue(cssSelect.getReplaceString(), textList);
                    result.put(cssSelect.getKey(), cssSelect.isConvertToArray() ? List.of(rv) : rv);
                    continue;
                }

                if (textList.size() == 1) {
                    result.put(cssSelect.getKey(), cssSelect.isConvertToArray() ? List.of(textList.getFirst()) : textList.getFirst());
                } else if (textList.size() > 1) {
                    result.put(cssSelect.getKey(), textList);
                } else {
                    result.put(cssSelect.getKey(), cssSelect.isConvertToArray() ? List.of() : "");
                }
            }
        } catch (Exception e) {
            throw new LsbException(e.getMessage());
        }
    }

}
