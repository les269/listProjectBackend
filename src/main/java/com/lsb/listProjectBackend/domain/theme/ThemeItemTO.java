package com.lsb.listProjectBackend.domain.theme;

import java.time.LocalDateTime;

import tools.jackson.databind.JsonNode;
import com.lsb.listProjectBackend.utils.Global;

public record ThemeItemTO(String itemId, Global.ThemeItemType type, String description, LocalDateTime updatedTime,
        JsonNode json) {
}
