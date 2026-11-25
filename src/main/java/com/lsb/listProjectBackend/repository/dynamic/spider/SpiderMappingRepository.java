package com.lsb.listProjectBackend.repository.dynamic.spider;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderMapping;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderMappingPK;

import java.util.List;

public interface SpiderMappingRepository extends JpaRepository<SpiderMapping, SpiderMappingPK> {
    List<SpiderMapping> findBySpiderIdOrderByExecutionOrderAsc(String spiderId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM SpiderMapping m WHERE m.spiderId = :spiderId")
    void deleteAllBySpiderId(@Param("spiderId") String spiderId);

    @Query(value = "SELECT * FROM spider_mapping  WHERE spider_id = :spiderId AND execution_order = :executionOrder AND spider_item_id = :spiderItemId", nativeQuery = true)
    SpiderMapping findBySpiderIdAndExecutionOrderAndSpiderItemId(@Param("spiderId") String spiderId,
            @Param("executionOrder") int executionOrder,
            @Param("spiderItemId") String spiderItemId);

    @Query(value = "SELECT COUNT(*) FROM spider_mapping m WHERE m.spider_item_id = :spiderItemId", nativeQuery = true)
    int countBySpiderItemId(@Param("spiderItemId") String spiderItemId);
}
