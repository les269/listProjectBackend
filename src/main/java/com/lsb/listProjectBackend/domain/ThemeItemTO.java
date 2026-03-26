package com.lsb.listProjectBackend.domain;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;
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
