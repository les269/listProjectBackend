package com.lsb.listProjectBackend.converter.theme;
import com.lsb.listProjectBackend.converter.common.JsonAttributeConverter;

import tools.jackson.core.type.TypeReference;

import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeImage;

import jakarta.persistence.Converter;

@Converter
public class ThemeImageConverter extends JsonAttributeConverter<ThemeImage> {
    @Override
    protected TypeReference<ThemeImage> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
