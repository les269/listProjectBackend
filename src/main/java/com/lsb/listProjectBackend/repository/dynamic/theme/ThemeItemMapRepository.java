package com.lsb.listProjectBackend.repository.dynamic.theme;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeItemMap;
import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeItemMapPK;

public interface ThemeItemMapRepository extends JpaRepository<ThemeItemMap, ThemeItemMapPK> {
    @Query(value = "SELECT COUNT(*) FROM theme_item_map WHERE item_id = :itemId AND type = :type", nativeQuery = true)
    int existsByItemIdAndType(@Param("itemId") String itemId, @Param("type") String type);

    @Query(value = "SELECT * FROM theme_item_map WHERE type = :type", nativeQuery = true)
    List<ThemeItemMap> findAllByType(@Param("type") String type);

    @Query(value = "SELECT * FROM theme_item_map WHERE header_id = :headerId", nativeQuery = true)
    List<ThemeItemMap> findAllByHeaderId(@Param("headerId") String headerId);
}
