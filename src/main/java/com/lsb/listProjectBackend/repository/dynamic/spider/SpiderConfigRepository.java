package com.lsb.listProjectBackend.repository.dynamic.spider;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderConfig;

public interface SpiderConfigRepository extends JpaRepository<SpiderConfig, String> {
}