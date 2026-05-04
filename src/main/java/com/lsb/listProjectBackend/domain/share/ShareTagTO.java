package com.lsb.listProjectBackend.domain.share;

import java.time.LocalDateTime;

public record ShareTagTO(String shareTagId, String shareTagName, LocalDateTime updatedTime) {
}