package com.lsb.listProjectBackend.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lsb.listProjectBackend.entity.dynamic.ScrapyData;

import java.util.List;

public class ScrapyDataListConverter extends JsonAttributeConverter<List<ScrapyData>> {
    @Override
    protected TypeReference<List<ScrapyData>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
