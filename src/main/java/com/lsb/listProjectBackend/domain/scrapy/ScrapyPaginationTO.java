package com.lsb.listProjectBackend.domain.scrapy;

import java.time.LocalDateTime;

import com.lsb.listProjectBackend.entity.dynamic.scrapy.ScrapyPaginationConfig;

public record ScrapyPaginationTO(String name, ScrapyPaginationConfig config, LocalDateTime createdTime,
        LocalDateTime updatedTime) {
}