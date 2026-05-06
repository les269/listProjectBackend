package com.lsb.listProjectBackend.entity.dynamic.spider;

import com.lsb.listProjectBackend.utils.Global;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class TimeFormatOption {
    private String format;
    private String formatParsed;
    @Enumerated(EnumType.STRING)
    private Global.Timezones timezones;
    private boolean skipTimezoneConversion;
}
