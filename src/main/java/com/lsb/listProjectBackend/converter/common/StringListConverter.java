package com.lsb.listProjectBackend.converter.common;

import tools.jackson.core.type.TypeReference;

import java.util.List;

public class StringListConverter  extends JsonAttributeConverter<List<String>>{
    @Override
    protected TypeReference<List<String>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}

