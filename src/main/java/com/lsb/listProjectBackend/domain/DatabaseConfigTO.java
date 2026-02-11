package com.lsb.listProjectBackend.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DatabaseConfigTO {
    private String configId;
    private String configName;
    private String databaseType;
    private String jdbcUrl;
    private String driverClassName;
    private String host;
    private Integer port;
    private String databaseName;
    private String username;
    private String password;
    private String hibernateDialect;
    private String additionalProperties;
    private Integer enabled;
    private String description;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
