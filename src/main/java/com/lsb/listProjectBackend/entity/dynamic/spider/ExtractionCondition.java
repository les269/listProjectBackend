package com.lsb.listProjectBackend.entity.dynamic.spider;

import com.lsb.listProjectBackend.utils.Global;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class ExtractionCondition {
    @Enumerated(EnumType.STRING)
    private Global.ExtractionStepCondition conditionType;
    private String key;
    private String value;
    private boolean ignoreCase;
}
