package com.lsb.listProjectBackend.domain.spider;

import java.time.LocalDateTime;

public record SpiderMappingTO(String spiderId, Integer executionOrder, String spiderItemId, LocalDateTime updatedTime) {
}