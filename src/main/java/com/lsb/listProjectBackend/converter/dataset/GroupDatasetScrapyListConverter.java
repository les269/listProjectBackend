package com.lsb.listProjectBackend.converter.dataset;
import com.lsb.listProjectBackend.converter.common.JsonAttributeConverter;

import tools.jackson.core.type.TypeReference;

import java.util.List;

import com.lsb.listProjectBackend.entity.dynamic.dataset.GroupDatasetScrapy;

public class GroupDatasetScrapyListConverter extends JsonAttributeConverter<List<GroupDatasetScrapy>> {
    @Override
    protected TypeReference<List<GroupDatasetScrapy>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
