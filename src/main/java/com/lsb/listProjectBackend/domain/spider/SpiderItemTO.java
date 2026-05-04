package com.lsb.listProjectBackend.domain.spider;

import java.time.LocalDateTime;

import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderItemSetting;

public record SpiderItemTO(String spiderItemId, String description, SpiderItemSetting itemSetting,
        LocalDateTime updatedTime) {
}