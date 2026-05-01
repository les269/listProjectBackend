package com.lsb.listProjectBackend.converter.spider;

import com.lsb.listProjectBackend.converter.common.JsonAttributeConverter;
import com.lsb.listProjectBackend.entity.dynamic.spider.InsertConfig;
import tools.jackson.core.type.TypeReference;

public class InsertConfigConverter extends JsonAttributeConverter<InsertConfig> {
    @Override
    protected TypeReference<InsertConfig> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}

