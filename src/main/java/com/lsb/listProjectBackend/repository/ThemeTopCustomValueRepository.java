package com.lsb.listProjectBackend.repository;

import com.lsb.listProjectBackend.entity.ThemeTopCustomValue;
import com.lsb.listProjectBackend.entity.ThemeTopCustomValuePK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ThemeTopCustomValueRepository  extends JpaRepository<ThemeTopCustomValue, ThemeTopCustomValuePK> {
    @Query(value = "select * from theme_top_custom_value where header_id=:headerId ", nativeQuery = true)
    List<ThemeTopCustomValue> findByHeaderId(@Param("headerId")String headerId);
}
