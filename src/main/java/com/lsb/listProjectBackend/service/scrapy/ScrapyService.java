package com.lsb.listProjectBackend.service.scrapy;

import com.lsb.listProjectBackend.domain.scrapy.ScrapyConfigTO;
import com.lsb.listProjectBackend.domain.scrapy.ScrapyReqTO;
import com.lsb.listProjectBackend.entity.dynamic.scrapy.CssSelect;
import com.lsb.listProjectBackend.entity.dynamic.scrapy.ScrapyData;

import java.util.List;
import java.util.Map;

public interface ScrapyService {
    List<ScrapyConfigTO> getAllConfig();

    void updateConfig(ScrapyConfigTO req);

    void deleteConfig(String name);

    ScrapyConfigTO getConfig(String name);

    boolean existConfig(String name);

    List<String> getAllName();

    List<ScrapyConfigTO> getByNameList(List<String> nameList);

    Map<String, Object> testParseHtml(String html, ScrapyData data);

    Map<String, Object> scrapyByJson(ScrapyReqTO to) throws Exception;

    Map<String, Object> doScrapyByJson(List<String> json, List<ScrapyData> scrapyDataList) throws Exception;

    Map<String, Object> scrapyByUrl(ScrapyReqTO to);

    Map<String, Object> doScrapyByUrl(String url, List<ScrapyData> scrapyDataList);

    void useCssSelect(String htmlString, List<CssSelect> select, Map<String, Object> result);
}
