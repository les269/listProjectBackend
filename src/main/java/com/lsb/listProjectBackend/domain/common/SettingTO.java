package com.lsb.listProjectBackend.domain.common;

import java.time.LocalDateTime;

public record SettingTO(String name, String description, String value, boolean enabled, LocalDateTime updatedTime,
        LocalDateTime createdTime) {
}
