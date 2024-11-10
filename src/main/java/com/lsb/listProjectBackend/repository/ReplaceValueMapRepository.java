package com.lsb.listProjectBackend.repository;

import com.lsb.listProjectBackend.entity.ReplaceValueMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReplaceValueMapRepository extends JpaRepository<ReplaceValueMap, String> {
    @Query(value = "select name,'{}' as map,created_time,updated_time from replace_value_map", nativeQuery = true)
    List<ReplaceValueMap> getNameList();
}
