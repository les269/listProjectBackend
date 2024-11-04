package com.lsb.listProjectBackend.converter;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;

public class MapObjectListConverter extends JsonAttributeConverter<List<Map<String,Object>>>{
    @Override
    protected TypeReference<List<Map<String, Object>>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
