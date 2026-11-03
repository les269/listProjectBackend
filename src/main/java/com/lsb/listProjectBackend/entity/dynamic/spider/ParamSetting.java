package com.lsb.listProjectBackend.entity.dynamic.spider;

import java.time.LocalDateTime;
import java.util.List;

import com.lsb.listProjectBackend.utils.Global;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class ParamSetting {
    private Integer seq;
    private String paramKey; // 置換的標記，如 {code}, {time}
    @Enumerated(EnumType.STRING)
    private Global.ParamType type; // STATIC_LIST, NUMBER_RANGE, TIME_RANGE

    // --- 1. 固定字串列表 ---
    private List<String> staticList;

    // --- 2. 數字推進 (全字串化，支援間隔與補零) ---
    private String startNumStr; // 例如 "001" 或 "0"
    private String endNumStr; // 例如 "999" 或 "100"
    private Integer numStep; // 步長間隔，例如 1 或 10
    private Boolean padZeros; // 是否保持與 startNumStr 同樣的長度補零

    // --- 3. 時間推進 (自由定義起點、終點與間隔單位) ---
    private LocalDateTime startTime;
    private String timeFormat; // 時間格式，例如 "yyyy-MM-dd HH:mm:ss"
    private Integer timeInterval; // 間隔數量，例如 1、2、7
    @Enumerated(EnumType.STRING)
    private Global.TimeUnit timeIntervalUnit;
    private EndTimeConfig endTimeConfig; // 終止時間設定 (固定、當前日期、當年最後一刻、相對偏移)
}
