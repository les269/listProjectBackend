package com.lsb.listProjectBackend.entity.dynamic;

import com.lsb.listProjectBackend.utils.Global;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class ScrapyPaginationConfig {
    private String startUrl;
    private Date lastUpdateDate;
    private Date currentUpdateDate;
    private Integer updateInterval;
    private Global.UpdateIntervalType updateIntervalType;
    private List<Cookie> cookie;
    private List<CssSelect> cssSelectList;
    private List<SpringExpressionLang> springExpressionLangList;
    private Map<String, String> keyRedirectUrlMap;
    // private List<List<String>> redirectParamsList;
}
