package com.lsb.listProjectBackend.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SettingTO {
    private String name;

    private String description;

    private String value;

    private boolean enabled;

    private LocalDateTime updatedTime;
}
