package com.lsb.listProjectBackend.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lsb.listProjectBackend.entity.dynamic.ThemeTopCustom;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class ThemeTopCustomListConverter extends JsonAttributeConverter<List<ThemeTopCustom>> {

    @Override
    protected TypeReference<List<ThemeTopCustom>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
