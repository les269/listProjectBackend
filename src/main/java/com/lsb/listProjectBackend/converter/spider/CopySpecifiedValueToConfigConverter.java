package com.lsb.listProjectBackend.converter.spider;

import com.lsb.listProjectBackend.converter.common.JsonAttributeConverter;
import com.lsb.listProjectBackend.entity.dynamic.spider.CopySpecifiedValueToConfig;
import tools.jackson.core.type.TypeReference;

public class CopySpecifiedValueToConfigConverter extends JsonAttributeConverter<CopySpecifiedValueToConfig> {
    @Override
    protected TypeReference<CopySpecifiedValueToConfig> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
