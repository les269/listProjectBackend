package com.lsb.listProjectBackend.domain.spider;

import com.lsb.listProjectBackend.domain.common.CookieListTO;
import com.lsb.listProjectBackend.domain.common.HeadersTO;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderPaginationConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Map;

/** 單次分頁執行的設定與請求狀態，各次執行獨立建立。 */
@Getter
@RequiredArgsConstructor
public class ExecutionContext {
    private final String spiderPaginationId;
    private final SpiderPaginationConfig config;
    private final HeadersTO headers;
    private final CookieListTO cookies;
    private final boolean force;

    @Setter
    private Map<String, Map<String, Object>> replaceValueMaps;

    @Setter
    private boolean hasRequested;
}
