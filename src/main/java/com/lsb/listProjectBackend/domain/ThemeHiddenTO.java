package com.lsb.listProjectBackend.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ThemeHiddenTO {
    private String headerId;
    private LocalDateTime updatedTime;
}
