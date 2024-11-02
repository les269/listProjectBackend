package com.lsb.listProjectBackend.repository;

import com.lsb.listProjectBackend.entity.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetRepository extends JpaRepository<Dataset, String> {
}
