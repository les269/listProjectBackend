package com.lsb.listProjectBackend.converter;

import com.fasterxml.jackson.core.type.TypeReference;

public class ObjectConverter extends JsonAttributeConverter<Object>{
    @Override
    protected TypeReference<Object> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
