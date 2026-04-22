package com.lsb.listProjectBackend.entity.dynamic;

import com.lsb.listProjectBackend.utils.Global;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.util.List;

@Data
public class SpiderItemSetting {
    private String url;

    @Enumerated(EnumType.STRING)
    private Global.SpiderUrlType urlType;

    private SpiderItemTestData testData;

    @Enumerated(EnumType.STRING)
    private Global.ExtractionRuleMode mode;

    private List<ExtractionRule> extractionRuleList;
    private boolean skipWhenUsingUrl;
    private boolean useCookie;
}
