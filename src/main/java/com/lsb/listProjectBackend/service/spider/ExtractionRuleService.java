package com.lsb.listProjectBackend.service.spider;

import com.jayway.jsonpath.DocumentContext;
import com.lsb.listProjectBackend.entity.dynamic.spider.ExtractionRule;
import com.lsb.listProjectBackend.entity.dynamic.spider.ValuePipeline;

import java.util.List;

public interface ExtractionRuleService {
    List<ExtractionRule> sortedRules(List<ExtractionRule> extractionRules);

    List<ValuePipeline> allPipelines(List<ExtractionRule> extractionRules);

    boolean checkCondition(ExtractionRule extractionRule, DocumentContext result);
}

