package com.lsb.listProjectBackend.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lsb.listProjectBackend.entity.dynamic.DatasetConfig;

public class DatasetConfigConverter extends JsonAttributeConverter<DatasetConfig> {
    @Override
    protected TypeReference<DatasetConfig> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
