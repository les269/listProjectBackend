package com.lsb.listProjectBackend.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lsb.listProjectBackend.entity.ThemeDB;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class ThemeDBListConverter extends JsonAttributeConverter<List<ThemeDB>> {

    @Override
    protected TypeReference<List<ThemeDB>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
