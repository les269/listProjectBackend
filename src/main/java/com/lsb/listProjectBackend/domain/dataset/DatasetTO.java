package com.lsb.listProjectBackend.domain.dataset;

import java.time.LocalDateTime;

import com.lsb.listProjectBackend.entity.dynamic.dataset.DatasetConfig;

public record DatasetTO(String name, DatasetConfig config, LocalDateTime createdTime, LocalDateTime updatedTime) {
}
