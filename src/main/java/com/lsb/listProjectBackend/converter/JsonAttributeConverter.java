package com.lsb.listProjectBackend.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;


@Converter
public abstract class JsonAttributeConverter<T> implements AttributeConverter<T,String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    protected abstract TypeReference<T> getTypeClass();

    @Override
    public String convertToDatabaseColumn(T attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    @Override
    public T convertToEntityAttribute(String s) {
        if(s == null){
            return null;
        }
        try {
            return objectMapper.readValue(s, getTypeClass());
        } catch (IOException e) {
            return null;
        }
    }


}
