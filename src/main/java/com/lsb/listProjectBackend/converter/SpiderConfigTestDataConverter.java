package com.lsb.listProjectBackend.converter;

import tools.jackson.core.type.TypeReference;
import com.lsb.listProjectBackend.entity.dynamic.SpiderConfigTestData;

public class SpiderConfigTestDataConverter extends JsonAttributeConverter<SpiderConfigTestData> {
    @Override
    protected TypeReference<SpiderConfigTestData> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
