package com.lsb.listProjectBackend.converter.theme;
import com.lsb.listProjectBackend.converter.common.JsonAttributeConverter;

import tools.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;

import java.util.List;

import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeDataset;

@Converter
public class ThemeDatasetListConverter extends JsonAttributeConverter<List<ThemeDataset>> {

    @Override
    protected TypeReference<List<ThemeDataset>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
