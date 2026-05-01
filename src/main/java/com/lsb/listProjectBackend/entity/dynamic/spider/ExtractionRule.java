package com.lsb.listProjectBackend.entity.dynamic.spider;

import lombok.Data;

import java.util.List;

import com.lsb.listProjectBackend.utils.Global;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Data
public class ExtractionRule {
    private Integer seq;
    private String key;
    private String selector;
    private String jsonPath;
    private List<ValuePipeline> pipelines;
    @Enumerated(EnumType.STRING)
    private Global.ExtractionStepCondition conditionType;
    private ExtractionCondition conditionValue;
}