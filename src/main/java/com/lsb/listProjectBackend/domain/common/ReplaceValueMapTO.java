package com.lsb.listProjectBackend.domain.common;

import java.time.LocalDateTime;
import java.util.Map;

public record ReplaceValueMapTO(String name, Map<String, Object> map, LocalDateTime createdTime,
        LocalDateTime updatedTime) {
}
