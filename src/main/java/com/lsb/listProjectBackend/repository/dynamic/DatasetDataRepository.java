package com.lsb.listProjectBackend.repository.dynamic;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lsb.listProjectBackend.entity.dynamic.DatasetData;

public interface DatasetDataRepository extends JpaRepository<DatasetData, String> {
}
