package com.lsb.listProjectBackend.converter.dataset;
import com.lsb.listProjectBackend.converter.common.JsonAttributeConverter;

import tools.jackson.core.type.TypeReference;

import java.util.List;

import com.lsb.listProjectBackend.entity.dynamic.dataset.GroupDatasetField;

public class GroupDatasetFieldListConverter extends JsonAttributeConverter<List<GroupDatasetField>> {
    @Override
    protected TypeReference<List<GroupDatasetField>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
