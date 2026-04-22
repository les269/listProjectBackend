package com.lsb.listProjectBackend.service;

import com.lsb.listProjectBackend.domain.SpiderItemTO;

import java.util.List;

public interface SpiderItemService {
    List<SpiderItemTO> getAll();

    SpiderItemTO getById(String spiderItemId);

    void update(SpiderItemTO req);

    void delete(String spiderItemId);

    boolean exists(String spiderItemId);

    List<SpiderItemTO> getByIdList(List<String> spiderItemIdList);

    List<SpiderItemTO> getBySpiderId(String spiderId);
}
