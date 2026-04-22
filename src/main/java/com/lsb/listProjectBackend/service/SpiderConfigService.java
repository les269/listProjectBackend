package com.lsb.listProjectBackend.service;

import com.lsb.listProjectBackend.domain.SpiderConfigTO;

import java.util.List;

public interface SpiderConfigService {
    List<SpiderConfigTO> getAll();

    SpiderConfigTO getById(String spiderId);

    void update(SpiderConfigTO req);

    void delete(String spiderId);

    boolean exists(String spiderId);
}
