package com.lsb.listProjectBackend.domain.share;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShareTagValueTO {
    private String shareTagId;
    private String value;
    private LocalDateTime updatedTime;
}
