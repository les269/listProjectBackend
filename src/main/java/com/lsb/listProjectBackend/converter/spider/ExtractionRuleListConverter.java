package com.lsb.listProjectBackend.converter.spider;
import com.lsb.listProjectBackend.converter.common.JsonAttributeConverter;

import tools.jackson.core.type.TypeReference;

import java.util.List;

import com.lsb.listProjectBackend.entity.dynamic.spider.ExtractionRule;

public class ExtractionRuleListConverter extends JsonAttributeConverter<List<ExtractionRule>> {
    @Override
    protected TypeReference<List<ExtractionRule>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}
