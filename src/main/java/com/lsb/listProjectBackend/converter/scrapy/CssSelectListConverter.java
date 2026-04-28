package com.lsb.listProjectBackend.converter.scrapy;
import com.lsb.listProjectBackend.converter.common.JsonAttributeConverter;

import tools.jackson.core.type.TypeReference;

import java.util.List;

import com.lsb.listProjectBackend.entity.dynamic.scrapy.CssSelect;

public class CssSelectListConverter extends JsonAttributeConverter<List<CssSelect>> {
    @Override
    protected TypeReference<List<CssSelect>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
