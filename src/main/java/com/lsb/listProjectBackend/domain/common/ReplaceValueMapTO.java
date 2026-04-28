package com.lsb.listProjectBackend.domain.common;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ReplaceValueMapTO {
    private String name;
    private Map<String, Object> map;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
