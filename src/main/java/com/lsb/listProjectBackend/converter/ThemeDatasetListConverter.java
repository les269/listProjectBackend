package com.lsb.listProjectBackend.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lsb.listProjectBackend.entity.dynamic.ThemeDataset;

import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class ThemeDatasetListConverter extends JsonAttributeConverter<List<ThemeDataset>> {

    @Override
    protected TypeReference<List<ThemeDataset>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
