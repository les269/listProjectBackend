package com.lsb.listProjectBackend.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lsb.listProjectBackend.entity.ThemeImage;
import jakarta.persistence.Converter;

import java.lang.reflect.Type;

@Converter
public class ThemeImageConverter extends JsonAttributeConverter<ThemeImage>{
    @Override
    protected TypeReference<ThemeImage> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
