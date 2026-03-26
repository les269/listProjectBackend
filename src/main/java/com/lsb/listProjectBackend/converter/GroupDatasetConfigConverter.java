package com.lsb.listProjectBackend.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lsb.listProjectBackend.entity.dynamic.GroupDatasetConfig;

public class GroupDatasetConfigConverter extends JsonAttributeConverter<GroupDatasetConfig> {
    @Override
    protected TypeReference<GroupDatasetConfig> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
