package com.lsb.listProjectBackend.converter.spider;
import com.lsb.listProjectBackend.converter.common.JsonAttributeConverter;

import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderItemSetting;

import tools.jackson.core.type.TypeReference;

public class SpiderItemSettingConverter extends JsonAttributeConverter<SpiderItemSetting> {
    @Override
    protected TypeReference<SpiderItemSetting> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
