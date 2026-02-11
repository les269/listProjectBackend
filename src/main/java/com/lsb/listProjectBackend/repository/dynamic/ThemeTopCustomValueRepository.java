package com.lsb.listProjectBackend.repository.dynamic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lsb.listProjectBackend.entity.dynamic.ThemeTopCustomValue;
import com.lsb.listProjectBackend.entity.dynamic.ThemeTopCustomValuePK;

import java.util.List;

public interface ThemeTopCustomValueRepository extends JpaRepository<ThemeTopCustomValue, ThemeTopCustomValuePK> {
    @Query(value = "select * from theme_top_custom_value where header_id=:headerId ", nativeQuery = true)
    List<ThemeTopCustomValue> findByHeaderId(@Param("headerId") String headerId);
}
