package com.lsb.listProjectBackend.repository.dynamic.common;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lsb.listProjectBackend.entity.dynamic.common.Headers;

public interface HeadersRepository extends JpaRepository<Headers, String> {

    @Query(value = "SELECT a.* FROM headers a " +
            "JOIN headers_map b ON a.headers_id = b.headers_id " +
            "WHERE b.ref_id = :refId AND b.type = :type", nativeQuery = true)
    Headers findByRefIdAndType(@Param("refId") String refId, @Param("type") String type);
}
