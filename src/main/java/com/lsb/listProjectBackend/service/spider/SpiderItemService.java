package com.lsb.listProjectBackend.service.spider;

import com.lsb.listProjectBackend.domain.spider.SpiderItemTO;

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
