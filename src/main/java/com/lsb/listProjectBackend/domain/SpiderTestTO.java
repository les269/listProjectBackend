package com.lsb.listProjectBackend.domain;

import lombok.Data;

import java.util.List;

@Data
public class SpiderTestTO {
    private SpiderConfigTO spiderConfig;
    private SpiderItemTO spiderItem;
    private List<SpiderItemTO> spiderItems;
}