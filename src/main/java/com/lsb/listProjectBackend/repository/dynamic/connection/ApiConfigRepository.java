package com.lsb.listProjectBackend.repository.dynamic.connection;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lsb.listProjectBackend.entity.dynamic.common.ApiConfig;

public interface ApiConfigRepository extends JpaRepository<ApiConfig, String> {
}
