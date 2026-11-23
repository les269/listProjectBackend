package com.lsb.listProjectBackend.domain.search;

import java.time.LocalDateTime;

public record SearchBookmarkItemTO(String id, String value, Integer seq, LocalDateTime createdTime,
        LocalDateTime updatedTime) {
}
