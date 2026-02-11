package com.lsb.listProjectBackend.repository.dynamic;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lsb.listProjectBackend.entity.dynamic.GroupDataset;

public interface GroupDatasetRepository extends JpaRepository<GroupDataset, String> {
}
