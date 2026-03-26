package com.lsb.listProjectBackend.repository.dynamic;

import com.lsb.listProjectBackend.entity.dynamic.ThemeItemMap;
import com.lsb.listProjectBackend.entity.dynamic.ThemeItemMapPK;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ThemeItemMapRepository extends JpaRepository<ThemeItemMap, ThemeItemMapPK> {
    @Query(value = "SELECT COUNT(*) FROM theme_item_map WHERE item_id = :itemId AND type = :type", nativeQuery = true)
    int existsByItemIdAndType(@Param("itemId") String itemId, @Param("type") String type);

    @Query(value = "SELECT * FROM theme_item_map WHERE header_id = :headerId", nativeQuery = true)
    List<ThemeItemMap> findAllByHeaderId(@Param("headerId") String headerId);
}
