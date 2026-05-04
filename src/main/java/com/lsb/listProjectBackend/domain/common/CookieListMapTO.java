package com.lsb.listProjectBackend.domain.common;

import com.lsb.listProjectBackend.utils.Global;

import java.time.LocalDateTime;

public record CookieListMapTO(String refId, Global.CookieListMapType type, String cookieId, LocalDateTime updatedTime) {
}
