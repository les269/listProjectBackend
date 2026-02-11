package com.lsb.listProjectBackend.repository.dynamic;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lsb.listProjectBackend.entity.dynamic.ScrapyConfig;

public interface ScrapyConfigRepository extends JpaRepository<ScrapyConfig, String> {
}
