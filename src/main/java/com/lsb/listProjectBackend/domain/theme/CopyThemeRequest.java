package com.lsb.listProjectBackend.domain.theme;

import lombok.Data;

@Data
public class CopyThemeRequest {
    private ThemeHeaderTO source;
    private ThemeHeaderTO target;
}
