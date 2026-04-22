package com.lsb.listProjectBackend.domain;

import com.lsb.listProjectBackend.entity.dynamic.Cookie;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class CookieListTO {
    private String cookieId;
    private List<Cookie> list;
    private String description;
    private LocalDateTime updatedTime;

    public Map<String, String> getCookieMap() {
        if (list == null) {
            return Map.of();
        }
        return list.stream().collect(Collectors.toMap(
                Cookie::getName,
                Cookie::getValue,
                (existingValue, newValue) -> newValue // 遇到重複時，繼續用新的值
        ));
    }
}
