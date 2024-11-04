package com.lsb.listProjectBackend.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lsb.listProjectBackend.entity.DatasetField;
import com.lsb.listProjectBackend.entity.GroupDatasetField;

import java.util.List;

public class GroupDatasetFieldListConverter extends JsonAttributeConverter<List<GroupDatasetField>>{
    @Override
    protected TypeReference<List<GroupDatasetField>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
