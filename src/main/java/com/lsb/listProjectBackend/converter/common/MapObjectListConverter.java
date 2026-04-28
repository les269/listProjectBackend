package com.lsb.listProjectBackend.converter.common;

import tools.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;

public class MapObjectListConverter extends JsonAttributeConverter<List<Map<String,Object>>>{
    @Override
    protected TypeReference<List<Map<String, Object>>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}

