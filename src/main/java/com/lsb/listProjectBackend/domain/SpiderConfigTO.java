package com.lsb.listProjectBackend.domain;

import com.lsb.listProjectBackend.entity.dynamic.SpiderConfigTestData;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SpiderConfigTO {
    private String spiderId;
    private String description;
    private Integer primeKeySize;
    private SpiderConfigTestData testData;
    private Boolean isUrlBased;
    private LocalDateTime updatedTime;
}
