package com.lsb.listProjectBackend.repository.dynamic.spider;

import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderPaginationData;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderPaginationDataPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpiderPaginationDataRepository extends JpaRepository<SpiderPaginationData, SpiderPaginationDataPK> {
    @Query(value = "SELECT * FROM spider_pagination_data WHERE spider_pagination_id = :spiderPaginationId", nativeQuery = true)
    List<SpiderPaginationData> findAllBySpiderPaginationId(@Param("spiderPaginationId") String spiderPaginationId);
}
