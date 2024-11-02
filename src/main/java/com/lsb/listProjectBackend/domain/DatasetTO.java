package com.lsb.listProjectBackend.domain;

import com.lsb.listProjectBackend.entity.DatasetConfig;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DatasetTO {
    private String name;
    private DatasetConfig config;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
