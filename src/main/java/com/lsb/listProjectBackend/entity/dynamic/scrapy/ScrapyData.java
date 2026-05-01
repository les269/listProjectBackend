package com.lsb.listProjectBackend.entity.dynamic.scrapy;

import com.lsb.listProjectBackend.entity.dynamic.common.Cookie;
import com.lsb.listProjectBackend.utils.Global;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.util.List;

@Data
public class ScrapyData {

    private String name;
    private String url;
    private List<Cookie> cookie;

    @Enumerated(EnumType.STRING)
    private Global.ScrapyPageType scrapyPageType;

    private List<CssSelect> cssSelectList;

    private String html;

    private String replaceRegular;
    private String replaceRegularTo;
}
