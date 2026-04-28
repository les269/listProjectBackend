package com.lsb.listProjectBackend.domain.spider;

import lombok.Data;

import java.time.LocalDateTime;

import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderItemSetting;

@Data
public class SpiderItemTO {
    private String spiderItemId;
    private String description;
    private SpiderItemSetting itemSetting;
    private LocalDateTime updatedTime;
}
