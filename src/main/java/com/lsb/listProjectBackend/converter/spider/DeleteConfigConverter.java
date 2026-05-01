package com.lsb.listProjectBackend.converter.spider;

import com.lsb.listProjectBackend.converter.common.JsonAttributeConverter;
import com.lsb.listProjectBackend.entity.dynamic.spider.DeleteConfig;
import tools.jackson.core.type.TypeReference;

public class DeleteConfigConverter extends JsonAttributeConverter<DeleteConfig> {
    @Override
    protected TypeReference<DeleteConfig> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
