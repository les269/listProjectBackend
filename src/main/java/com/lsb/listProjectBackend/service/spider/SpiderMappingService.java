package com.lsb.listProjectBackend.service.spider;

import com.lsb.listProjectBackend.domain.spider.SpiderMappingTO;

import java.util.List;

public interface SpiderMappingService {
    List<SpiderMappingTO> getAll();

    SpiderMappingTO getById(String spiderId, Integer executionOrder);

    List<SpiderMappingTO> getBySpiderId(String spiderId);

    void update(SpiderMappingTO req);

    void updateList(List<SpiderMappingTO> req);

    void delete(String spiderId, Integer executionOrder, String spiderItemId);

    void deleteBySpiderId(String spiderId);

    boolean inUseBySpiderItemId(String spiderItemId);
}
