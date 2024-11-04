package com.lsb.listProjectBackend.repository;

import com.lsb.listProjectBackend.entity.DatasetData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetDataRepository extends JpaRepository<DatasetData, String> {
}
