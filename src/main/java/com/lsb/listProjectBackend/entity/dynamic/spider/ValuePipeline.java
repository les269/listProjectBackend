package com.lsb.listProjectBackend.entity.dynamic.spider;

import com.lsb.listProjectBackend.converter.spider.CopySpecifiedValueToConfigConverter;
import com.lsb.listProjectBackend.converter.spider.DeleteConfigConverter;
import com.lsb.listProjectBackend.converter.spider.InsertConfigConverter;
import com.lsb.listProjectBackend.converter.spider.MoveCharConfigConverter;
import com.lsb.listProjectBackend.utils.Global;
import jakarta.persistence.Convert;
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
    private String attributeName;
    private String pattern;
    private String replacement;
    private String separator;
    private String combineToString;
    private String combineByKey;
    private String useReplaceValueMap;

    private List<String> mergeMultiObjKeys;
    private List<String> mergeMultiArrayKeys;
    @Enumerated(EnumType.STRING)
    private Global.ConvertToCaseType convertToCaseType;
    private CurrentTimeFormatOption currentTimeFormatOption;
    @Enumerated(EnumType.STRING)
    private Global.ChineseConvertType chineseConvertType;
    @Convert(converter = InsertConfigConverter.class)
    private InsertConfig insertConfig;
    @Convert(converter = CopySpecifiedValueToConfigConverter.class)
    private CopySpecifiedValueToConfig copySpecifiedValueToConfig;
    @Convert(converter = DeleteConfigConverter.class)
    private DeleteConfig deleteConfig;
    private List<String> deletePaths;
    @Convert(converter = MoveCharConfigConverter.class)
    private MoveCharConfig moveCharConfig;
    private String joinArraySeparator;

}