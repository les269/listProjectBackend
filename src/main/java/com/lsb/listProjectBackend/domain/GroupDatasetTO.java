package com.lsb.listProjectBackend.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import com.lsb.listProjectBackend.entity.dynamic.GroupDatasetConfig;

@Data
public class GroupDatasetTO {
    private String groupName;
    private GroupDatasetConfig config;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
