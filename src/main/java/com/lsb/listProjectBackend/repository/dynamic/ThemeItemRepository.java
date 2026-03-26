package com.lsb.listProjectBackend.repository.dynamic;

import com.lsb.listProjectBackend.entity.dynamic.ThemeItem;
import com.lsb.listProjectBackend.entity.dynamic.ThemeItemPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ThemeItemRepository extends JpaRepository<ThemeItem, ThemeItemPK> {
    @Query(value = "SELECT t.item_id, t.type, t.json,t.description, t.updated_time FROM theme_item t WHERE t.type = :type", nativeQuery = true)
    List<ThemeItem> findAllSummaryByType(@Param("type") String type);

    @Query(value = "SELECT t.* FROM theme_item t JOIN theme_item_map m ON t.item_id = m.item_id AND t.type = m.type WHERE m.header_id = :headerId", nativeQuery = true)
    List<ThemeItem> findAllByHeaderId(@Param("headerId") String headerId);
}
