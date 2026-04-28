package com.lsb.listProjectBackend.entity.dynamic.scrapy;

import com.lsb.listProjectBackend.converter.common.CookieListConverter;
import com.lsb.listProjectBackend.converter.scrapy.CssSelectListConverter;
import com.lsb.listProjectBackend.entity.dynamic.common.Cookie;
import com.lsb.listProjectBackend.utils.Global;
import jakarta.persistence.Convert;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.util.List;

@Data
public class ScrapyData {

    private String name;
    private String url;

    @Convert(converter = CookieListConverter.class)
    private List<Cookie> cookie;

    @Enumerated(EnumType.STRING)
    private Global.ScrapyPageType scrapyPageType;

    @Convert(converter = CssSelectListConverter.class)
    private List<CssSelect> cssSelectList;

    private String html;

    private String replaceRegular;
    private String replaceRegularTo;
}
