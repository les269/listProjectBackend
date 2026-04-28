package com.lsb.listProjectBackend.service.spider;

import com.lsb.listProjectBackend.domain.spider.SpiderConfigTO;

import java.util.List;

public interface SpiderConfigService {
    List<SpiderConfigTO> getAll();

    SpiderConfigTO getById(String spiderId);

    void update(SpiderConfigTO req);

    void delete(String spiderId);

    boolean exists(String spiderId);
}
