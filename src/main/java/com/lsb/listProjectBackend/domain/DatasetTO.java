package com.lsb.listProjectBackend.domain;

import lombok.Data;

import java.time.LocalDateTime;

import com.lsb.listProjectBackend.entity.dynamic.DatasetConfig;

@Data
public class DatasetTO {
    private String name;
    private DatasetConfig config;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
