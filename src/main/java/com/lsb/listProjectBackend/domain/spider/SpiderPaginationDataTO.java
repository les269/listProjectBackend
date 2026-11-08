package com.lsb.listProjectBackend.domain.spider;

import java.time.LocalDateTime;
import java.util.Map;

public record SpiderPaginationDataTO(String spiderPaginationId, String groupId, String dataId, Map<String, Object> data,
        LocalDateTime createdTime, LocalDateTime updatedTime, LocalDateTime lastSuccessTime) {
}
