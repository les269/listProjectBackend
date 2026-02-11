package com.lsb.listProjectBackend.repository.dynamic;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lsb.listProjectBackend.entity.dynamic.ApiConfig;

public interface ApiConfigRepository extends JpaRepository<ApiConfig, String> {
}
