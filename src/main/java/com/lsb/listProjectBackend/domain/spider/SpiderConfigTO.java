package com.lsb.listProjectBackend.domain.spider;

import java.time.LocalDateTime;

import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderConfigTestData;

public record SpiderConfigTO(String spiderId, String description, Integer primeKeySize, SpiderConfigTestData testData,
        Boolean isUrlBased, LocalDateTime updatedTime) {
}