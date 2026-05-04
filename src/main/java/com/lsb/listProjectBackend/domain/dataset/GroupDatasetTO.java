package com.lsb.listProjectBackend.domain.dataset;

import java.time.LocalDateTime;

import com.lsb.listProjectBackend.entity.dynamic.dataset.GroupDatasetConfig;

public record GroupDatasetTO(String groupName, GroupDatasetConfig config, LocalDateTime createdTime,
        LocalDateTime updatedTime) {
}
