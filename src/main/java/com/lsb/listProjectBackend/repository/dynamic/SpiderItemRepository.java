package com.lsb.listProjectBackend.repository.dynamic;

import com.lsb.listProjectBackend.entity.dynamic.SpiderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpiderItemRepository extends JpaRepository<SpiderItem, String> {

    @Query(value = "SELECT si.* FROM spider_item si "
            + "INNER JOIN spider_mapping sm ON si.spider_item_id = sm.spider_item_id "
            + "WHERE sm.spider_id = :spiderId "
            + "ORDER BY sm.execution_order", nativeQuery = true)
    List<SpiderItem> findAllBySpiderId(@Param("spiderId") String spiderId);
}