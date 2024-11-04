package com.lsb.listProjectBackend.repository;

import com.lsb.listProjectBackend.entity.GroupDatasetData;
import com.lsb.listProjectBackend.entity.GroupDatasetDataPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupDatasetDataRepository extends JpaRepository<GroupDatasetData, GroupDatasetDataPK> {
    @Query(value = "select * from group_dataset_data where group_name=:groupName", nativeQuery = true)
    List<GroupDatasetData> findByGroupName(@Param("groupName")String groupName);
}
