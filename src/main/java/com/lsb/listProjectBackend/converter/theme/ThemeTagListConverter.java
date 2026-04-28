package com.lsb.listProjectBackend.converter.theme;
import com.lsb.listProjectBackend.converter.common.JsonAttributeConverter;

import tools.jackson.core.type.TypeReference;

import java.util.List;

import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeTag;

public class ThemeTagListConverter extends JsonAttributeConverter<List<ThemeTag>> {
    @Override
    protected TypeReference<List<ThemeTag>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
