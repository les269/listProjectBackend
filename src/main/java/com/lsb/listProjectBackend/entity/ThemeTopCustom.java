package com.lsb.listProjectBackend.entity;

import com.lsb.listProjectBackend.utils.Global;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class ThemeTopCustom {
    @Enumerated(EnumType.STRING)
    private Global.ThemeTopCustomType type;
    private String byKey;

    private String label;
    private String seq;
    private String openUrl;
    private ApiConfig apiConfig;
}
