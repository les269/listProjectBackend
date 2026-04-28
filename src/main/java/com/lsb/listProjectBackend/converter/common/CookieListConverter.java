package com.lsb.listProjectBackend.converter.common;

import tools.jackson.core.type.TypeReference;

import java.util.List;

import com.lsb.listProjectBackend.entity.dynamic.common.Cookie;

public class CookieListConverter extends JsonAttributeConverter<List<Cookie>> {
    @Override
    protected TypeReference<List<Cookie>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
