package com.lsb.listProjectBackend.domain.connection;

public record ConnectionTestReqTO(
        String databaseType,
        String sqliteFilePath,
        String host,
        Integer port,
        String databaseName,
        String username,
        String password) {
}
