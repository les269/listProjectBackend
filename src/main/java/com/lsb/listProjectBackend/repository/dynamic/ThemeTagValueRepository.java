package com.lsb.listProjectBackend.repository.dynamic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lsb.listProjectBackend.entity.dynamic.ThemeTagValue;
import com.lsb.listProjectBackend.entity.dynamic.ThemeTagValuePK;

import java.util.List;

public interface ThemeTagValueRepository extends JpaRepository<ThemeTagValue, ThemeTagValuePK> {
    @Query(value = "select * from theme_tag_value where header_id=:headerId", nativeQuery = true)
    List<ThemeTagValue> findByHeaderId(@Param("headerId") String headerId);
}
