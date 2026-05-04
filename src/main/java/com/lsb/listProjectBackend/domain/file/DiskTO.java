package com.lsb.listProjectBackend.domain.file;

import java.time.LocalDateTime;

public record DiskTO(String disk, Long totalSpace, Long freeSpace, Long usableSpace, LocalDateTime updateDate) {
}