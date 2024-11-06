package com.lsb.listProjectBackend.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lsb.listProjectBackend.entity.ThemeTag;

import java.util.List;

public class ThemeTagListConverter extends JsonAttributeConverter<List<ThemeTag>> {
    @Override
    protected TypeReference<List<ThemeTag>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
