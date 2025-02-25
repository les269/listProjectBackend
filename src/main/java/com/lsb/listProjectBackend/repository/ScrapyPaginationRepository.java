package com.lsb.listProjectBackend.repository;

import com.lsb.listProjectBackend.entity.ScrapyPagination;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScrapyPaginationRepository extends JpaRepository<ScrapyPagination, String> {
}
