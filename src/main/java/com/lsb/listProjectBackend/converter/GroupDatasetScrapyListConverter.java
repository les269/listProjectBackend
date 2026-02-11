package com.lsb.listProjectBackend.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lsb.listProjectBackend.entity.dynamic.GroupDatasetScrapy;

import java.util.List;

public class GroupDatasetScrapyListConverter extends JsonAttributeConverter<List<GroupDatasetScrapy>> {
    @Override
    protected TypeReference<List<GroupDatasetScrapy>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
