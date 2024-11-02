package com.lsb.listProjectBackend.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lsb.listProjectBackend.entity.CssSelect;

import java.util.List;

public class ObjectListConverter extends JsonAttributeConverter<List<Object>>{
    @Override
    protected TypeReference<List<Object>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
