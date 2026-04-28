package com.lsb.listProjectBackend.domain.theme;

import java.time.LocalDateTime;

import com.lsb.listProjectBackend.utils.Global;

import lombok.Data;

@Data
public class ThemeItemMapTO {
    private String headerId;
    private Global.ThemeItemType type;
    private String itemId;
    private LocalDateTime updatedTime;
}
