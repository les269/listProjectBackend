package com.lsb.listProjectBackend.repository.dynamic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lsb.listProjectBackend.entity.dynamic.GroupDatasetData;
import com.lsb.listProjectBackend.entity.dynamic.GroupDatasetDataPK;

import java.util.List;

public interface GroupDatasetDataRepository extends JpaRepository<GroupDatasetData, GroupDatasetDataPK> {
    @Query(value = "select * from group_dataset_data where group_name=:groupName", nativeQuery = true)
    List<GroupDatasetData> findByGroupName(@Param("groupName") String groupName);

    @Query(value = "select '' as group_name,prime_value,'{}' as json,created_time,updated_time from group_dataset_data where group_name=:groupName", nativeQuery = true)
    List<GroupDatasetData> getAllGroupDatasetDataOnlyPrimeValue(@Param("groupName") String groupName);
}
