package com.lsb.listProjectBackend.service.spider;

import com.lsb.listProjectBackend.domain.spider.SpiderPaginationDataTO;

import java.util.List;

public interface SpiderPaginationDataService {
    List<SpiderPaginationDataTO> getAll();

    List<SpiderPaginationDataTO> getBySpiderPaginationId(String spiderPaginationId);

    SpiderPaginationDataTO getById(String spiderPaginationId, String dataId);

    void update(SpiderPaginationDataTO req);

    void delete(String spiderPaginationId, String dataId);

    void deleteBySpiderPaginationId(String spiderPaginationId);

    boolean exists(String spiderPaginationId, String dataId);
}
