package com.lsb.listProjectBackend.entity.dynamic.spider;

import com.lsb.listProjectBackend.utils.Global;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class EndTimeConfig {
    @Enumerated(EnumType.STRING)
    private Global.EndTimeType endTimeType; // FIXED, CURRENT_DATE, END_CURRENT_YEAR, RELATIVE_OFFSET
    private String timeFormat;

    // 當 strategy == FIXED 時使用 (必須符合外層的 timeFormat)
    private String endTimeStr;

    // 當 strategy == RELATIVE_OFFSET 時使用 (例如：當前時間再減 7 天)
    private Integer offsetInterval; // 可以是負數或正數，例如 -7
    private Global.TimeStepUnit offsetUnit; // DAY, WEEK, MONTH...
}
