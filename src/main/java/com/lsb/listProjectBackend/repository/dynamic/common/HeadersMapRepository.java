package com.lsb.listProjectBackend.repository.dynamic.common;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lsb.listProjectBackend.entity.dynamic.common.HeadersMap;
import com.lsb.listProjectBackend.entity.dynamic.common.HeadersMapPK;

import java.util.List;

public interface HeadersMapRepository extends JpaRepository<HeadersMap, HeadersMapPK> {

    @Query(value = "SELECT * FROM headers_map WHERE ref_id = :refId", nativeQuery = true)
    List<HeadersMap> findAllByRefId(@Param("refId") String refId);

    @Query(value = "SELECT * FROM headers_map WHERE ref_id IN (:refIds) AND type = :type", nativeQuery = true)
    List<HeadersMap> findAllByRefIdsAndType(@Param("refIds") List<String> refIds, @Param("type") String type);

    @Query(value = "SELECT * FROM headers_map WHERE type = :type", nativeQuery = true)
    List<HeadersMap> findAllByType(@Param("type") String type);

    @Query(value = "SELECT count(*) FROM headers_map WHERE headers_id = :headersId", nativeQuery = true)
    int headersIsInUse(@Param("headersId") String headersId);
}
