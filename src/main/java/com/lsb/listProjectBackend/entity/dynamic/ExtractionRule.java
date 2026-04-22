package com.lsb.listProjectBackend.entity.dynamic;

import com.lsb.listProjectBackend.utils.Global;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.util.List;

@Data
public class ExtractionRule {
    private Integer seq;
    private String key;
    private String selector;
    private String jsonPath;
    private List<ValuePipeline> pipelines;
    @Enumerated(EnumType.STRING)
    private Global.ExtractionStepCondition condition;
    private String conditionKey;
    private String conditionValue;
}