package com.lsb.listProjectBackend.entity.dynamic.spider;

import java.util.List;

import com.lsb.listProjectBackend.utils.Global;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class SpiderPaginationConfig {
    private String dataIdTemplate;
    private List<ParamSetting> perStartParams;
    private String startUrl;
    private String nextPageUrlKey; // 當資料為空時即終止
    @Enumerated(EnumType.STRING)
    private Global.ExtractionRuleMode extractionRuleMode;
    private List<ExtractionRule> extractionRuleList;
    private UpdateControl updateControl;
    private Long requestIntervalMs;
}
