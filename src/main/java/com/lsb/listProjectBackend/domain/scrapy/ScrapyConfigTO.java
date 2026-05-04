package com.lsb.listProjectBackend.domain.scrapy;

import java.time.LocalDateTime;
import java.util.List;

import com.lsb.listProjectBackend.entity.dynamic.scrapy.ScrapyData;

public record ScrapyConfigTO(String name, Integer paramSize, List<ScrapyData> data, LocalDateTime createdTime,
        LocalDateTime updatedTime, String testJson, String testUrl) {
}