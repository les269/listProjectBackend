package com.lsb.listProjectBackend.repository.dynamic.dataset;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lsb.listProjectBackend.entity.dynamic.dataset.DatasetData;

public interface DatasetDataRepository extends JpaRepository<DatasetData, String> {
}
