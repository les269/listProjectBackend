package com.lsb.listProjectBackend.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lsb.listProjectBackend.entity.dynamic.ThemeLabel;

import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class ThemeLabelListConverter extends JsonAttributeConverter<List<ThemeLabel>> {
    @Override
    protected TypeReference<List<ThemeLabel>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
