package com.lsb.listProjectBackend.repository;

import com.lsb.listProjectBackend.entity.ShareTagValue;
import com.lsb.listProjectBackend.entity.ShareTagValuePK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShareTagValueRepository extends JpaRepository<ShareTagValue, ShareTagValuePK> {
    @Query(value = "SELECT * FROM share_tag_value WHERE share_tag_id IN (:shareTagIds)", nativeQuery = true)
    List<ShareTagValue> findByShareTagIdIn(@Param("shareTagIds") List<String> shareTagIds);

    @Query(value = "SELECT * FROM share_tag_value WHERE share_tag_id = :shareTagId", nativeQuery = true)
    List<ShareTagValue> findByShareTagId(@Param("shareTagId") String shareTagId);
}
