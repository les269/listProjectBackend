package com.lsb.listProjectBackend.entity.dynamic.spider;

import lombok.Data;

import java.util.List;

@Data
public class ExtractionRule {
    private Integer seq;
    private String key;
    private String selector;
    private String jsonPath;
    private List<ValuePipeline> pipelines;

    private ExtractionCondition conditionValue;
}