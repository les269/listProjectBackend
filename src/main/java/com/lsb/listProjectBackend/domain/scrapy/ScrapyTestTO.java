package com.lsb.listProjectBackend.domain.scrapy;

import lombok.Data;

import java.util.List;

import com.lsb.listProjectBackend.entity.dynamic.scrapy.ScrapyData;

@Data
public class ScrapyTestTO {
    private ScrapyData scrapyData;
    private List<ScrapyData> scrapyDataList;
    private List<String> json;
    private String url;
}
