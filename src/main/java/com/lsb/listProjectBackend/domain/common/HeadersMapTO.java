package com.lsb.listProjectBackend.domain.common;

import com.lsb.listProjectBackend.utils.Global;

import java.time.LocalDateTime;

public record HeadersMapTO(String refId, Global.HeadersMapType type, String headersId, LocalDateTime updatedTime) {
}
