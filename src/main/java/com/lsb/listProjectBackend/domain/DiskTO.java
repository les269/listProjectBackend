package com.lsb.listProjectBackend.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DiskTO {
    private String disk;

    private Long totalSpace;

    private Long freeSpace;

    private Long usableSpace;

    private LocalDateTime updateDate;
}
