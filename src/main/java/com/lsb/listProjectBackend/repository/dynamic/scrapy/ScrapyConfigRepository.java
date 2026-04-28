package com.lsb.listProjectBackend.repository.dynamic.scrapy;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lsb.listProjectBackend.entity.dynamic.scrapy.ScrapyConfig;

public interface ScrapyConfigRepository extends JpaRepository<ScrapyConfig, String> {
}
