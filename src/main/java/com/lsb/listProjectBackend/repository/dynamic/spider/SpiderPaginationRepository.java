package com.lsb.listProjectBackend.repository.dynamic.spider;

import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderPagination;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpiderPaginationRepository extends JpaRepository<SpiderPagination, String> {
}
