package com.lsb.listProjectBackend.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lsb.listProjectBackend.entity.dynamic.ThemeCustom;

import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class ThemeCustomListConverter extends JsonAttributeConverter<List<ThemeCustom>> {

    @Override
    protected TypeReference<List<ThemeCustom>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
