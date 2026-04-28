package com.lsb.listProjectBackend.converter.scrapy;
import com.lsb.listProjectBackend.converter.common.JsonAttributeConverter;

import com.lsb.listProjectBackend.entity.dynamic.scrapy.ScrapyPaginationConfig;

import tools.jackson.core.type.TypeReference;

public class ScrapyPaginationConfigConverter extends JsonAttributeConverter<ScrapyPaginationConfig> {
    @Override
    protected TypeReference<ScrapyPaginationConfig> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
