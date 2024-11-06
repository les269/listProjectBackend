package com.lsb.listProjectBackend.repository;

import com.lsb.listProjectBackend.entity.ThemeTagValue;
import com.lsb.listProjectBackend.entity.ThemeTagValuePK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ThemeTagValueRepository extends JpaRepository<ThemeTagValue, ThemeTagValuePK> {
    @Query(value = "select * from theme_tag_value where header_id=:headerId", nativeQuery = true)
    List<ThemeTagValue> findByHeaderId(@Param("headerId")String headerId);
}
