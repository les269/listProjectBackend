package com.lsb.listProjectBackend.repository.dynamic.common;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lsb.listProjectBackend.entity.dynamic.common.CookieListMap;
import com.lsb.listProjectBackend.entity.dynamic.common.CookieListMapPK;

import java.util.List;

public interface CookieListMapRepository extends JpaRepository<CookieListMap, CookieListMapPK> {

    @Query(value = "SELECT * FROM cookie_list_map WHERE ref_id = :refId", nativeQuery = true)
    List<CookieListMap> findAllByRefId(@Param("refId") String refId);

    @Query(value = "SELECT * FROM cookie_list_map WHERE ref_id IN (:refIds) AND type = :type", nativeQuery = true)
    List<CookieListMap> findAllByRefIdsAndType(@Param("refIds") List<String> refIds, @Param("type") String type);

    @Query(value = "SELECT * FROM cookie_list_map WHERE type = :type", nativeQuery = true)
    List<CookieListMap> findAllByType(@Param("type") String type);

    @Query(value = "SELECT count(*) FROM cookie_list_map WHERE cookie_id = :cookieId", nativeQuery = true)
    int cookieIsInUse(@Param("cookieId") String cookieId);
}
