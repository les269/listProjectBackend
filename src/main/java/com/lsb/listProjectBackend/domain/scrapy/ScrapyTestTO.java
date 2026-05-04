package com.lsb.listProjectBackend.domain.scrapy;

import java.util.List;

import com.lsb.listProjectBackend.entity.dynamic.scrapy.ScrapyData;

public record ScrapyTestTO(ScrapyData scrapyData, List<ScrapyData> scrapyDataList, List<String> json, String url) {
}
