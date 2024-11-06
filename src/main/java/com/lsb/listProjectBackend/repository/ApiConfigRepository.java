package com.lsb.listProjectBackend.repository;

import com.lsb.listProjectBackend.entity.ApiConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiConfigRepository extends JpaRepository<ApiConfig, String> {
}
