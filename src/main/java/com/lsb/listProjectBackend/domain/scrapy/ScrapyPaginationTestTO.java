package com.lsb.listProjectBackend.domain.scrapy;

import com.lsb.listProjectBackend.entity.dynamic.scrapy.ScrapyPaginationConfig;

public record ScrapyPaginationTestTO(String html, ScrapyPaginationConfig config) {
}
