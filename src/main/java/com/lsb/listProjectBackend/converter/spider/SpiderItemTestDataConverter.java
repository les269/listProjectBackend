package com.lsb.listProjectBackend.converter.spider;
import com.lsb.listProjectBackend.converter.common.JsonAttributeConverter;

import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderItemTestData;

import tools.jackson.core.type.TypeReference;

public class SpiderItemTestDataConverter extends JsonAttributeConverter<SpiderItemTestData> {
    @Override
    protected TypeReference<SpiderItemTestData> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
