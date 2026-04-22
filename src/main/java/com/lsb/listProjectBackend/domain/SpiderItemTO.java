package com.lsb.listProjectBackend.domain;

import com.lsb.listProjectBackend.entity.dynamic.SpiderItemSetting;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SpiderItemTO {
    private String spiderItemId;
    private String description;
    private SpiderItemSetting itemSetting;
    private LocalDateTime updatedTime;
}
