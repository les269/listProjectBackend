package com.lsb.listProjectBackend.repository.local;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lsb.listProjectBackend.entity.local.DatabaseConfig;

public interface DatabaseConfigRepository extends JpaRepository<DatabaseConfig, String> {

    Optional<DatabaseConfig> findByConfigName(String configName);

    @Query("SELECT d FROM DatabaseConfig d WHERE d.enabled = 1")
    List<DatabaseConfig> findAllEnabled();

    @Query("SELECT d FROM DatabaseConfig d WHERE d.databaseType = :databaseType AND d.enabled = 1")
    List<DatabaseConfig> findByDatabaseTypeAndEnabled(@Param("databaseType") String databaseType);
}
