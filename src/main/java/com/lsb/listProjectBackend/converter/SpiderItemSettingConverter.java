package com.lsb.listProjectBackend.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lsb.listProjectBackend.entity.dynamic.SpiderItemSetting;

public class SpiderItemSettingConverter extends JsonAttributeConverter<SpiderItemSetting> {
    @Override
    protected TypeReference<SpiderItemSetting> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
