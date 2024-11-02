package com.lsb.listProjectBackend.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lsb.listProjectBackend.entity.DatasetScrapy;

import java.util.List;

public class DatasetScrapyListConverter  extends JsonAttributeConverter<List<DatasetScrapy>>{
    @Override
    protected TypeReference<List<DatasetScrapy>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
