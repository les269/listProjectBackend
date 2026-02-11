package com.lsb.listProjectBackend.repository.dynamic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lsb.listProjectBackend.entity.dynamic.ShareTagValue;
import com.lsb.listProjectBackend.entity.dynamic.ShareTagValuePK;

import java.util.List;

public interface ShareTagValueRepository extends JpaRepository<ShareTagValue, ShareTagValuePK> {
    @Query(value = "SELECT * FROM share_tag_value WHERE share_tag_id IN (:shareTagIds)", nativeQuery = true)
    List<ShareTagValue> findByShareTagIdIn(@Param("shareTagIds") List<String> shareTagIds);

    @Query(value = "SELECT * FROM share_tag_value WHERE share_tag_id = :shareTagId", nativeQuery = true)
    List<ShareTagValue> findByShareTagId(@Param("shareTagId") String shareTagId);
}
