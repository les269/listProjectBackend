package com.lsb.listProjectBackend.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lsb.listProjectBackend.entity.Cookie;

import java.util.List;

public class CookieListConverter extends JsonAttributeConverter<List<Cookie>>{
    @Override
    protected TypeReference<List<Cookie>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
