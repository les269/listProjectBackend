package com.lsb.listProjectBackend.service.spider;

import com.lsb.listProjectBackend.domain.common.ReplaceValueMapTO;
import com.lsb.listProjectBackend.entity.dynamic.spider.ValuePipeline;
import com.lsb.listProjectBackend.entity.dynamic.spider.ValuePipelineContext;

import java.util.List;

public interface ValuePipelineService {
    List<ReplaceValueMapTO> fetchReplaceValueMaps(List<ValuePipeline> pipelines);

    boolean hasEnabledPipelines(List<ValuePipeline> pipelines);

    Object applyPipelines(List<ValuePipeline> pipelines, Object pipelineValue, ValuePipelineContext context);

    Object applyPipeline(ValuePipeline pipeline, Object pipelineValue, ValuePipelineContext context);

    Object normalizeExtractedValue(Object pipelineValue);

}


