package com.lsb.listProjectBackend.domain;

import lombok.Data;

@Data
public class SettingTO {
    private String name;

    private String description;

    private String value;

    private boolean enabled;
}
