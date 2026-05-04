package com.lsb.listProjectBackend.domain.theme;

import java.time.LocalDateTime;

import com.lsb.listProjectBackend.utils.Global;

public record ThemeItemMapTO(String headerId, Global.ThemeItemType type, String itemId, LocalDateTime updatedTime) {
}
