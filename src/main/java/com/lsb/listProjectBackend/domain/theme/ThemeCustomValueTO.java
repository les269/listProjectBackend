package com.lsb.listProjectBackend.domain.theme;

import lombok.Data;

@Data
public class ThemeCustomValueTO {
    private String headerId;
    private String byKey;
    private String correspondDataValue;
    private String customValue;
}
