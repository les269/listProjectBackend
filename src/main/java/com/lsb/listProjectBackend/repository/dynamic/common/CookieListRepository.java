package com.lsb.listProjectBackend.repository.dynamic.common;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lsb.listProjectBackend.entity.dynamic.common.CookieList;

public interface CookieListRepository extends JpaRepository<CookieList, String> {
    @Query(value = "SELECT a.* FROM cookie_list a " +
            "JOIN cookie_list_map b ON a.cookie_id = b.cookie_id " +
            "WHERE b.ref_id = :refId AND b.type = :type", nativeQuery = true)
    CookieList findByRefIdAndType(@Param("refId") String refId, @Param("type") String type);

}
