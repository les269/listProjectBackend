package com.lsb.listProjectBackend.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lsb.listProjectBackend.entity.ScrapyPaginationConfig;

import java.util.List;

public class ScrapyPaginationConfigConverter extends JsonAttributeConverter<ScrapyPaginationConfig>{
    @Override
    protected TypeReference<ScrapyPaginationConfig> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
