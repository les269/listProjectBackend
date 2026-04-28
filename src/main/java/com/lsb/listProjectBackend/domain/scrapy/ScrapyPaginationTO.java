package com.lsb.listProjectBackend.domain.scrapy;

import lombok.Data;

import java.time.LocalDateTime;

import com.lsb.listProjectBackend.entity.dynamic.scrapy.ScrapyPaginationConfig;

@Data
public class ScrapyPaginationTO {
    private String name;
    private ScrapyPaginationConfig config;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
