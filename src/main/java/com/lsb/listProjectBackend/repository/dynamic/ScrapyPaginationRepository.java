package com.lsb.listProjectBackend.repository.dynamic;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lsb.listProjectBackend.entity.dynamic.ScrapyPagination;

public interface ScrapyPaginationRepository extends JpaRepository<ScrapyPagination, String> {
}
