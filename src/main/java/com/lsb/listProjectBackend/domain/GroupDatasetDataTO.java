package com.lsb.listProjectBackend.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class GroupDatasetDataTO {
    private String groupName;
    private String primeValue;
    private Map<String,Object> json;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
