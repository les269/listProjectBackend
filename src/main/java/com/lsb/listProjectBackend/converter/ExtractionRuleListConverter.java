package com.lsb.listProjectBackend.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lsb.listProjectBackend.entity.dynamic.ExtractionRule;

import java.util.List;

public class ExtractionRuleListConverter extends JsonAttributeConverter<List<ExtractionRule>> {
    @Override
    protected TypeReference<List<ExtractionRule>> getTypeClass() {
        return new TypeReference<>() {
        };
    }
}