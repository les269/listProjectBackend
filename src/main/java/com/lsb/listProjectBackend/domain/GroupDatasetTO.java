package com.lsb.listProjectBackend.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GroupDatasetTO {
    private String groupName;
    private String primeValue;
    private Object json;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
