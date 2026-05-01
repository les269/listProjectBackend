package com.lsb.listProjectBackend.converter.spider;

import com.lsb.listProjectBackend.converter.common.JsonAttributeConverter;
import com.lsb.listProjectBackend.entity.dynamic.spider.MoveCharConfig;
import tools.jackson.core.type.TypeReference;

public class MoveCharConfigConverter extends JsonAttributeConverter<MoveCharConfig> {
    @Override
    protected TypeReference<MoveCharConfig> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
