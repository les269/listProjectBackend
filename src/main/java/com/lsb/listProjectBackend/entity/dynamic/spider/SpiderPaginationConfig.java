package com.lsb.listProjectBackend.entity.dynamic.spider;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.lsb.listProjectBackend.utils.Global;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class SpiderPaginationConfig {
    private String groupIdTemplate;
    private String dataIdKey;
    private List<ParamSetting> paramSettings;
    private String startUrl;
    private String nextPageUrlKey; // 當資料為空時即終止
    @Enumerated(EnumType.STRING)
    private Global.ExtractionRuleMode extractionRuleMode;
    private List<ExtractionRule> extractionRuleList;
    private UpdateControl updateControl;
    private Long requestIntervalMs;
    // false 時，同步移除成功抓取的參數組中已不存在的資料；省略或傳入 null 時保留預設值。
    @JsonSetter(nulls = Nulls.SKIP)
    private boolean retainHistory = true;
}
