package com.lsb.listProjectBackend.domain.common;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.lsb.listProjectBackend.entity.dynamic.common.Cookie;

@Data
public class CookieListTO {
    private String cookieId;
    private List<Cookie> list;
    private String description;
    private LocalDateTime updatedTime;

}
