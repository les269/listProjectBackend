package com.lsb.listProjectBackend.entity.dynamic.spider;

import java.time.LocalDateTime;

import com.lsb.listProjectBackend.utils.Global;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class UpdateControl {
    @Enumerated(EnumType.STRING)
    private Global.UpdateControlMode lastUpdateMode;
    private LocalDateTime lastUpdateDate;
    private String lastUpdateParamKey;

    @Enumerated(EnumType.STRING)
    private Global.UpdateControlMode updateIntervalMode;
    private Integer updateInterval;
    @Enumerated(EnumType.STRING)
    private Global.UpdateIntervalUnit updateIntervalUnit;
    private String updateIntervalParamKey;
}
