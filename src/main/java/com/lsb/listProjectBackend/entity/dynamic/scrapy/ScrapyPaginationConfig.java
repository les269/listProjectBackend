package com.lsb.listProjectBackend.entity.dynamic.scrapy;

import com.lsb.listProjectBackend.entity.dynamic.common.Cookie;
import com.lsb.listProjectBackend.utils.Global;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ScrapyPaginationConfig {
    private String startUrl;

    private Long lastUpdateDate;
    private Long currentUpdateDate;
    private Integer updateInterval;
    private Global.UpdateIntervalType updateIntervalType;
    private List<Cookie> cookie;
    private List<CssSelect> cssSelectList;
    private List<SpringExpressionLang> springExpressionLangList;
    private Map<String, String> keyRedirectUrlMap;
    // private List<List<String>> redirectParamsList;
}
