package com.lsb.listProjectBackend.domain.scrapy;

import com.lsb.listProjectBackend.entity.dynamic.scrapy.ScrapyPaginationConfig;

import lombok.Data;

@Data
public class ScrapyPaginationTestTO {
    private String html;
    private ScrapyPaginationConfig config;
}
