package com.lsb.listProjectBackend.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lsb.listProjectBackend.entity.GroupDatasetApi;

import java.util.List;

public class GroupDatasetApiListConverter extends JsonAttributeConverter<List<GroupDatasetApi>>{
    @Override
    protected TypeReference<List<GroupDatasetApi>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
