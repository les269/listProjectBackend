package com.lsb.listProjectBackend.domain;

import com.lsb.listProjectBackend.entity.dynamic.ScrapyPaginationConfig;

import lombok.Data;

@Data
public class ScrapyPaginationTestTO {
    private String html;
    private ScrapyPaginationConfig config;
}
