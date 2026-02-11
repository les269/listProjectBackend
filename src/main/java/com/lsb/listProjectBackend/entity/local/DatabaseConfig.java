package com.lsb.listProjectBackend.entity.local;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "database_config")
public class DatabaseConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "config_id")
    private String configId;

    @Column(name = "config_name", unique = true, nullable = false)
    private String configName;

    @Column(name = "database_type", nullable = false)
    private String databaseType;

    @Column(name = "jdbc_url", nullable = false)
    private String jdbcUrl;

    @Column(name = "driver_class_name", nullable = false)
    private String driverClassName;

    @Column(name = "host")
    private String host;

    @Column(name = "port")
    private Integer port;

    @Column(name = "database_name")
    private String databaseName;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "hibernate_dialect")
    private String hibernateDialect;

    @Column(name = "additional_properties")
    private String additionalProperties;

    @Column(name = "enabled", nullable = false)
    private Integer enabled = 1;

    @Column(name = "description")
    private String description;

    @CreationTimestamp
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;
}
