package com.lsb.listProjectBackend.domain.search;

import java.time.LocalDateTime;

public record SearchBookmarkTO(String id, String name, LocalDateTime createdTime, LocalDateTime updatedTime) {
}
