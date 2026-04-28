package com.lsb.listProjectBackend.domain.spider;

import lombok.Data;

import java.time.LocalDateTime;

import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderConfigTestData;

@Data
public class SpiderConfigTO {
    private String spiderId;
    private String description;
    private Integer primeKeySize;
    private SpiderConfigTestData testData;
    private Boolean isUrlBased;
    private LocalDateTime updatedTime;
}
