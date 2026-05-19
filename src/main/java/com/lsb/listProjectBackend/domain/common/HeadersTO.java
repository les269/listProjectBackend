package com.lsb.listProjectBackend.domain.common;

import java.time.LocalDateTime;
import java.util.List;

import com.lsb.listProjectBackend.entity.dynamic.common.Header;

public record HeadersTO(String headersId, List<Header> list, String description, LocalDateTime updatedTime) {
}
