package com.lsb.listProjectBackend.converter.scrapy;
import com.lsb.listProjectBackend.converter.common.JsonAttributeConverter;

import tools.jackson.core.type.TypeReference;

import java.util.List;

import com.lsb.listProjectBackend.entity.dynamic.scrapy.ScrapyData;

public class ScrapyDataListConverter extends JsonAttributeConverter<List<ScrapyData>> {
    @Override
    protected TypeReference<List<ScrapyData>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
