package com.lsb.listProjectBackend.converter.spider;
import com.lsb.listProjectBackend.converter.common.JsonAttributeConverter;

import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderConfigTestData;

import tools.jackson.core.type.TypeReference;

public class SpiderConfigTestDataConverter extends JsonAttributeConverter<SpiderConfigTestData> {
    @Override
    protected TypeReference<SpiderConfigTestData> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
