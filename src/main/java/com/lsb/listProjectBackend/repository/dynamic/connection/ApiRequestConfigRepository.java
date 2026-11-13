package com.lsb.listProjectBackend.repository.dynamic.connection;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lsb.listProjectBackend.entity.dynamic.common.ApiRequestConfig;

public interface ApiRequestConfigRepository extends JpaRepository<ApiRequestConfig, String> {
}
