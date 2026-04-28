package com.lsb.listProjectBackend.converter.dataset;
import com.lsb.listProjectBackend.converter.common.JsonAttributeConverter;

import tools.jackson.core.type.TypeReference;

import java.util.List;

import com.lsb.listProjectBackend.entity.dynamic.dataset.GroupDatasetApi;

public class GroupDatasetApiListConverter extends JsonAttributeConverter<List<GroupDatasetApi>> {
    @Override
    protected TypeReference<List<GroupDatasetApi>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
