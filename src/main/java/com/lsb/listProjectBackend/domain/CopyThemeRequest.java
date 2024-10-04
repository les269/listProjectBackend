package com.lsb.listProjectBackend.domain;

import lombok.Data;

@Data
public class CopyThemeRequest {
    private ThemeHeaderTO source;
    private ThemeHeaderTO target;
}
