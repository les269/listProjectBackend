package com.lsb.listProjectBackend.entity;

import com.lsb.listProjectBackend.utils.Global;
import jakarta.persistence.*;
import lombok.Data;

@Data
public class ThemeCustom  {

    @Enumerated(EnumType.STRING)
    private Global.ThemeCustomType type;
    private String byKey;

    private String label;
    private String seq;
    private String openUrl;
    private String openUrlByKey;
    private String copyValue;
    private String copyValueByKey;
    private String buttonIconFill;
    private String buttonIconFillColor;
    private String buttonIconTrue;
    private String buttonIconFalse;
    private ApiConfig apiConfig;

}
