package com.lsb.listProjectBackend.repository;

import com.lsb.listProjectBackend.entity.ThemeCustomValue;
import com.lsb.listProjectBackend.entity.ThemeCustomValuePK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ThemeCustomValueRepository extends JpaRepository<ThemeCustomValue, ThemeCustomValuePK> {
    @Query(value = "select * from theme_custom_value where header_id=:headerId and correspond_data_value in (:valueList)", nativeQuery = true)
    List<ThemeCustomValue> findByHeaderIdAndInCorrDataValue(@Param("headerId")String headerId,@Param("valueList") List<String> valueList);
}
