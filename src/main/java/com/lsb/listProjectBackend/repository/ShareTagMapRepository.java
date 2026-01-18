package com.lsb.listProjectBackend.repository;

import com.lsb.listProjectBackend.entity.ShareTagMap;
import com.lsb.listProjectBackend.entity.ShareTagMapPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShareTagMapRepository extends JpaRepository<ShareTagMap, ShareTagMapPK> {
    @Query(value = "SELECT * FROM share_tag_map WHERE theme_header_id = :themeHeaderId", nativeQuery = true)
    List<ShareTagMap> findByThemeHeaderId(@Param("themeHeaderId") String themeHeaderId);

    @Query(value = "SELECT CASE WHEN EXISTS (SELECT 1 FROM share_tag_map WHERE share_tag_id = :shareTagId) THEN 1 ELSE 0 END", nativeQuery = true)
    int existsByShareTagId(@Param("shareTagId") String shareTagId);

    @Query(value = "SELECT theme_header_id FROM share_tag_map WHERE share_tag_id = :shareTagId", nativeQuery = true)
    List<String> findThemeHeaderIdsByShareTagId(@Param("shareTagId") String shareTagId);
}
