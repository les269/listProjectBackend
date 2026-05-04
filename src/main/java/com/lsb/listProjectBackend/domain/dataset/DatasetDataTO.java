package com.lsb.listProjectBackend.domain.dataset;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record DatasetDataTO(String datasetName, List<Map<String, Object>> data, LocalDateTime createdTime,
        LocalDateTime updatedTime) {
}
