package com.lsb.listProjectBackend.domain.spider;

import java.util.List;

public record SpiderTestTO(SpiderConfigTO spiderConfig, List<SpiderItemTO> spiderItems) {
}