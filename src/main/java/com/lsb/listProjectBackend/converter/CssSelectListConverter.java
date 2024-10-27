package com.lsb.listProjectBackend.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lsb.listProjectBackend.entity.CssSelect;

import java.util.List;

public class CssSelectListConverter extends JsonAttributeConverter<List<CssSelect>>{
    @Override
    protected TypeReference<List<CssSelect>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
