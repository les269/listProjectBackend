package com.lsb.listProjectBackend.converter.dataset;
import com.lsb.listProjectBackend.converter.common.JsonAttributeConverter;

import com.lsb.listProjectBackend.entity.dynamic.dataset.DatasetConfig;

import tools.jackson.core.type.TypeReference;

public class DatasetConfigConverter extends JsonAttributeConverter<DatasetConfig> {
    @Override
    protected TypeReference<DatasetConfig> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
