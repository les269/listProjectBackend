package com.lsb.listProjectBackend.converter;

import tools.jackson.core.type.TypeReference;
import com.lsb.listProjectBackend.entity.dynamic.GroupDatasetField;

import java.util.List;

public class GroupDatasetFieldListConverter extends JsonAttributeConverter<List<GroupDatasetField>> {
    @Override
    protected TypeReference<List<GroupDatasetField>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}

