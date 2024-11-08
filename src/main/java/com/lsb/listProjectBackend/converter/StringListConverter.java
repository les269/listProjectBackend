package com.lsb.listProjectBackend.converter;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class StringListConverter  extends JsonAttributeConverter<List<String>>{
    @Override
    protected TypeReference<List<String>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
