package com.lsb.listProjectBackend.entity.dynamic.spider;

import java.time.LocalDateTime;

import com.lsb.listProjectBackend.utils.Global;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class EndTimeConfig {
    @Enumerated(EnumType.STRING)
    private Global.EndTimeType endTimeType; // FIXED, CURRENT_DATE, END_CURRENT_YEAR, RELATIVE_OFFSET
    private String timeFormat;

    // 當 endTimeType == FIXED 時使用
    private LocalDateTime endTime;

    // 當 endTimeType == RELATIVE_OFFSET 時使用
    private Integer offsetInterval; // 可以是負數或正數，例如 -7
    @Enumerated(EnumType.STRING)
    private Global.TimeUnit offsetUnit; // DAY, WEEK, MONTH...
}
