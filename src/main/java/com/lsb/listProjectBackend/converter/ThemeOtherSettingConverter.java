package com.lsb.listProjectBackend.converter;


import com.fasterxml.jackson.core.type.TypeReference;
import com.lsb.listProjectBackend.entity.ThemeOtherSetting;

public class ThemeOtherSettingConverter extends JsonAttributeConverter<ThemeOtherSetting>{
    @Override
    protected TypeReference<ThemeOtherSetting> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
