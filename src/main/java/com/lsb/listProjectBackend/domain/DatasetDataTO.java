package com.lsb.listProjectBackend.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class DatasetDataTO {
    private String datasetName;
    private List<Map<String,Object>> data;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
