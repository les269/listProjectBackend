package com.lsb.listProjectBackend.domain.theme;

import java.time.LocalDateTime;

import tools.jackson.databind.JsonNode;
import com.lsb.listProjectBackend.utils.Global;

import lombok.Data;

@Data
public class ThemeItemTO {
    private String itemId;
    private Global.ThemeItemType type;
    private String description;
    private LocalDateTime updatedTime;
    private JsonNode json;
}

