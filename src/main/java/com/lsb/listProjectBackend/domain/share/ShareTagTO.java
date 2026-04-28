package com.lsb.listProjectBackend.domain.share;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShareTagTO {
    private String shareTagId;
    private String shareTagName;
    private LocalDateTime updatedTime;
}
