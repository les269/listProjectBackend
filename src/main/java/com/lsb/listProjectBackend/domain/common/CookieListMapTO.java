package com.lsb.listProjectBackend.domain.common;

import com.lsb.listProjectBackend.utils.Global;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CookieListMapTO {
    private String refId;
    private Global.CookieListMapType type;
    private String cookieId;
    private LocalDateTime updatedTime;
}
