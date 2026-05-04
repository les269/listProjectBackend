package com.lsb.listProjectBackend.domain.share;

import java.time.LocalDateTime;

public record ShareTagValueTO(String shareTagId, String value, LocalDateTime updatedTime) {
}
