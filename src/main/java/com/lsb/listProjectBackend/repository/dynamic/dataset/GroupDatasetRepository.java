package com.lsb.listProjectBackend.repository.dynamic.dataset;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lsb.listProjectBackend.entity.dynamic.dataset.GroupDataset;

public interface GroupDatasetRepository extends JpaRepository<GroupDataset, String> {
}
