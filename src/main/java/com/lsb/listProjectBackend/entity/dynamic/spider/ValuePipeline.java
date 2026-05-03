package com.lsb.listProjectBackend.entity.dynamic.spider;

import com.lsb.listProjectBackend.utils.Global;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.util.List;

@Data
public class ValuePipeline {
    private int seq;

    @Enumerated(EnumType.STRING)
    private Global.ValuePipelineType type;

    private boolean enabled;
    private String fixedValue;
    private String fixedJsonValue;
    private String currentDataKey;
    private String attributeName;
    private String pattern;
    private String replacement;
    private String separator;
    private String joinSeparator;
    private String combineToString;
    private String combineByKey;
    private String useReplaceValueMap;

    private List<String> mergeMultiObjKeys;
    private List<String> mergeMultiArrayKeys;
    @Enumerated(EnumType.STRING)
    private Global.ConvertToCaseType convertToCaseType;
    private CurrentTimeFormatOption currentTimeFormatOption;
    private ChineseConvert chineseConvert;
    private InsertConfig insertConfig;
    private CopySpecifiedValueToConfig copySpecifiedValueToConfig;
    private DeleteConfig deleteConfig;
    private List<String> deletePaths;
    private MoveCharConfig moveCharConfig;
    private String joinArraySeparator;

}