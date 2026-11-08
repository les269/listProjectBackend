package com.lsb.listProjectBackend.service.spider;

import com.lsb.listProjectBackend.domain.spider.SpiderPaginationDataTO;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderPaginationDataPK;

import java.util.List;

public interface SpiderPaginationDataService {
    List<SpiderPaginationDataTO> getAll();

    List<SpiderPaginationDataTO> getBySpiderPaginationId(String spiderPaginationId);

    SpiderPaginationDataTO getById(String spiderPaginationId, String groupId, String dataId);

    void update(SpiderPaginationDataTO req);

    List<SpiderPaginationDataTO> updateAll(List<SpiderPaginationDataTO> records);

    /**
     * 在同一個交易內刪除失效資料並批次寫入本次結果。
     *
     * @param records 本次待新增或更新的 pagination 資料
     * @param idsToDelete 待刪除舊資料的複合主鍵，每筆包含 spiderPaginationId、groupId、dataId
     * @return 儲存後的資料
     */
    List<SpiderPaginationDataTO> synchronize(List<SpiderPaginationDataTO> records,
            List<SpiderPaginationDataPK> idsToDelete);

    void delete(String spiderPaginationId, String groupId, String dataId);

    void deleteBySpiderPaginationId(String spiderPaginationId);

    boolean exists(String spiderPaginationId, String groupId, String dataId);
}
