package com.lsb.listProjectBackend.domain.common;

import java.time.LocalDateTime;
import java.util.List;

import com.lsb.listProjectBackend.entity.dynamic.common.Cookie;

public record CookieListTO(String cookieId, List<Cookie> list, String description, LocalDateTime updatedTime) {
}
