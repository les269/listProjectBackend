package com.lsb.listProjectBackend.repository;

import com.lsb.listProjectBackend.entity.ThemeDB;
import com.lsb.listProjectBackend.entity.ThemeDBPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ThemeDBRepository extends JpaRepository<ThemeDB, ThemeDBPK> {

    @Modifying
    @Query(value = "delete from theme_db where header_id = :headerId", nativeQuery = true)
    void deleteByHeaderId(@Param("headerId") String headerId);
}
