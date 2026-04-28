package com.lsb.listProjectBackend.domain.spider;

import lombok.Data;

import java.util.List;

@Data
public class SpiderTestTO {
    private SpiderConfigTO spiderConfig;
    private List<SpiderItemTO> spiderItems;
}