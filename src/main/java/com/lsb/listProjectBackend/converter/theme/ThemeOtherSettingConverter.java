package com.lsb.listProjectBackend.converter.theme;
import com.lsb.listProjectBackend.converter.common.JsonAttributeConverter;

import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeOtherSetting;

import tools.jackson.core.type.TypeReference;

public class ThemeOtherSettingConverter extends JsonAttributeConverter<ThemeOtherSetting> {
    @Override
    protected TypeReference<ThemeOtherSetting> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
