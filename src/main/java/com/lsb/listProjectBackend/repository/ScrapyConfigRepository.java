package com.lsb.listProjectBackend.repository;

import com.lsb.listProjectBackend.entity.ScrapyConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScrapyConfigRepository extends JpaRepository<ScrapyConfig, String> {
}
