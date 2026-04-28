package com.lsb.listProjectBackend.converter.dataset;
import com.lsb.listProjectBackend.converter.common.JsonAttributeConverter;

import tools.jackson.core.type.TypeReference;

import java.util.List;

import com.lsb.listProjectBackend.entity.dynamic.dataset.DatasetField;

public class DatasetFieldListConverter extends JsonAttributeConverter<List<DatasetField>> {
    @Override
    protected TypeReference<List<DatasetField>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
