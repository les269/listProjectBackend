package com.lsb.listProjectBackend.entity.dynamic;

import com.lsb.listProjectBackend.utils.Global;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class ValuePipeline {
    private int seq;
    @Enumerated(EnumType.STRING)
    private Global.ValuePipelineType type;
    private boolean enabled;
    private String attributeName;
    private String pattern;
    private String replacement;
    private String separator;
    private String combineToString;
    private String combineByKey;
    private String useReplaceValueMap;
}