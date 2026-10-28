package com.lsb.listProjectBackend.domain.spider;

import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderPaginationConfig;

import java.time.LocalDateTime;

public record SpiderPaginationTO(String spiderPaginationId, SpiderPaginationConfig paginationConfig,
        LocalDateTime createdTime, LocalDateTime updatedTime) {
}
