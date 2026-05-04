package com.lsb.listProjectBackend.domain.dataset;

import java.time.LocalDateTime;
import java.util.Map;

public record GroupDatasetDataTO(String groupName, String primeValue, Map<String, Object> json,
        LocalDateTime createdTime, LocalDateTime updatedTime) {
}
