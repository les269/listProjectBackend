package com.lsb.listProjectBackend.converter;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public abstract class JsonAttributeConverter<T> implements AttributeConverter<T, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    protected abstract TypeReference<T> getTypeClass();

    @Override
    public String convertToDatabaseColumn(T attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public T convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        try {
            return objectMapper.readValue(s, getTypeClass());
        } catch (Exception e) {
            return null;
        }
    }


}




