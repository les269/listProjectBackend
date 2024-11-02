package com.lsb.listProjectBackend.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lsb.listProjectBackend.entity.DatasetField;

import java.util.List;

public class DatasetFieldListConverter extends JsonAttributeConverter<List<DatasetField>>{
    @Override
    protected TypeReference<List<DatasetField>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
