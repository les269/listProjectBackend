package com.lsb.listProjectBackend.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lsb.listProjectBackend.entity.dynamic.ScrapyPaginationConfig;

import java.util.List;

public class ScrapyPaginationConfigConverter extends JsonAttributeConverter<ScrapyPaginationConfig> {
    @Override
    protected TypeReference<ScrapyPaginationConfig> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
