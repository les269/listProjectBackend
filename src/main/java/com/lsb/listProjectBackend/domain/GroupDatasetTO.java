package com.lsb.listProjectBackend.domain;

import com.lsb.listProjectBackend.entity.GroupDatasetConfig;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GroupDatasetTO {
    private String groupName;
    private GroupDatasetConfig config;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
