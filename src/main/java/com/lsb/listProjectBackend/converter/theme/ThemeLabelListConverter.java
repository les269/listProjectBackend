package com.lsb.listProjectBackend.converter.theme;
import com.lsb.listProjectBackend.converter.common.JsonAttributeConverter;

import tools.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;

import java.util.List;

import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeLabel;

@Converter
public class ThemeLabelListConverter extends JsonAttributeConverter<List<ThemeLabel>> {
    @Override
    protected TypeReference<List<ThemeLabel>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
