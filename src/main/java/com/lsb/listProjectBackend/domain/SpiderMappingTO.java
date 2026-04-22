package com.lsb.listProjectBackend.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SpiderMappingTO {
    private String spiderId;
    private Integer executionOrder;
    private String spiderItemId;
    private LocalDateTime updatedTime;
}
