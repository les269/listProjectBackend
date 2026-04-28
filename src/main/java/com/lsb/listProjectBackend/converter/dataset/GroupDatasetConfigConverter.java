package com.lsb.listProjectBackend.converter.dataset;
import com.lsb.listProjectBackend.converter.common.JsonAttributeConverter;

import com.lsb.listProjectBackend.entity.dynamic.dataset.GroupDatasetConfig;

import tools.jackson.core.type.TypeReference;

public class GroupDatasetConfigConverter extends JsonAttributeConverter<GroupDatasetConfig> {
    @Override
    protected TypeReference<GroupDatasetConfig> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
