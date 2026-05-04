package com.lsb.listProjectBackend.domain.connection;

import java.time.LocalDateTime;

public record DatabaseConfigTO(
        String configId,
        String configName,
        String databaseType,
        String jdbcUrl,
        String driverClassName,
        String host,
        Integer port,
        String databaseName,
        String username,
        String password,
        String hibernateDialect,
        String additionalProperties,
        Integer enabled,
        String description,
        LocalDateTime createdTime,
        LocalDateTime updatedTime) {
}
