package com.lsb.listProjectBackend.converter;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;

public class MapObjectConverter extends JsonAttributeConverter<Map<String,Object>>{
    @Override
    protected TypeReference<Map<String,Object>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
