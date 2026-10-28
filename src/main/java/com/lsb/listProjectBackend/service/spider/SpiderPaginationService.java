package com.lsb.listProjectBackend.service.spider;

import com.lsb.listProjectBackend.domain.spider.SpiderPaginationTO;

import java.util.List;

public interface SpiderPaginationService {
    List<SpiderPaginationTO> getAll();

    SpiderPaginationTO getById(String spiderPaginationId);

    void update(SpiderPaginationTO req);

    void delete(String spiderPaginationId);

    boolean exists(String spiderPaginationId);
}
