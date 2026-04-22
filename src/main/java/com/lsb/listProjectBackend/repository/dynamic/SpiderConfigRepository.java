package com.lsb.listProjectBackend.repository.dynamic;

import com.lsb.listProjectBackend.entity.dynamic.SpiderConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpiderConfigRepository extends JpaRepository<SpiderConfig, String> {
}