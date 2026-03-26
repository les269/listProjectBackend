package com.lsb.listProjectBackend.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lsb.listProjectBackend.entity.dynamic.ThemeImage;

import jakarta.persistence.Converter;

@Converter
public class ThemeImageConverter extends JsonAttributeConverter<ThemeImage> {
    @Override
    protected TypeReference<ThemeImage> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
