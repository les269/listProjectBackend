package com.lsb.listProjectBackend.converter;

import tools.jackson.core.type.TypeReference;
import com.lsb.listProjectBackend.entity.dynamic.SpiderItemTestData;

public class SpiderItemTestDataConverter extends JsonAttributeConverter<SpiderItemTestData> {
    @Override
    protected TypeReference<SpiderItemTestData> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
