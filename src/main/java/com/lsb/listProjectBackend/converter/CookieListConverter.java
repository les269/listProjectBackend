package com.lsb.listProjectBackend.converter;

import tools.jackson.core.type.TypeReference;
import com.lsb.listProjectBackend.entity.dynamic.Cookie;

import java.util.List;

public class CookieListConverter extends JsonAttributeConverter<List<Cookie>> {
    @Override
    protected TypeReference<List<Cookie>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}

