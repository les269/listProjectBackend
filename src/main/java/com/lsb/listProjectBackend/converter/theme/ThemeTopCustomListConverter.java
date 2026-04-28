package com.lsb.listProjectBackend.converter.theme;
import com.lsb.listProjectBackend.converter.common.JsonAttributeConverter;

import tools.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;

import java.util.List;

import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeTopCustom;

@Converter
public class ThemeTopCustomListConverter extends JsonAttributeConverter<List<ThemeTopCustom>> {

    @Override
    protected TypeReference<List<ThemeTopCustom>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
