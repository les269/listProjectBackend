package com.lsb.listProjectBackend.service.connection;

import com.lsb.listProjectBackend.domain.connection.ConnectionTestReqTO;
import com.lsb.listProjectBackend.domain.connection.ConnectionTestResultTO;
import com.lsb.listProjectBackend.domain.connection.DatabaseConfigTO;
import com.lsb.listProjectBackend.mapper.connection.DatabaseConfigMapper;
import com.lsb.listProjectBackend.repository.local.connection.DatabaseConfigRepository;
import com.lsb.listProjectBackend.service.connection.DatabaseConfigService;
import com.lsb.listProjectBackend.utils.Global;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

/**
 * local不需要 @UseDynamic or @UseDynamicTx
 */
@Service
@RequiredArgsConstructor
public class DatabaseConfigServiceImpl implements DatabaseConfigService {
    private final DatabaseConfigRepository databaseConfigRepository;

    private final DatabaseConfigMapper databaseConfigMapper;

    @Override
    public List<DatabaseConfigTO> getAll() {
        return databaseConfigMapper.toDomainList(databaseConfigRepository.findAll());
    }

    @Override
    public DatabaseConfigTO getById(String configId) {
        return databaseConfigMapper.toDomain(databaseConfigRepository.findById(configId).orElse(null));
    }

    @Override
    public ConnectionTestResultTO testConnection(ConnectionTestReqTO to) {
        String type = to.databaseType();
        String sqliteFilePath = to.sqliteFilePath();
        String jdbcUrl = "";

        if ("sqlite".equalsIgnoreCase(type)) {
            if (sqliteFilePath == null || sqliteFilePath.isBlank()) {
                return new ConnectionTestResultTO(false, "sqliteFilePath required for sqlite");
            }
            File file = new File(sqliteFilePath);

            if (!file.exists() || !file.isFile()) {
                return new ConnectionTestResultTO(false, "sqliteFilePath not exist or not file");
            }
            jdbcUrl = "jdbc:sqlite:" + file.getAbsolutePath();
        } else if ("postgresql".equalsIgnoreCase(type) || "postgres".equalsIgnoreCase(type)) {
            String host = to.host() == null ? "localhost" : to.host();
            Integer port = to.port() == null ? 5432 : to.port();
            String db = to.databaseName() == null ? "" : to.databaseName();
            if (db.isBlank() || host.isBlank()) {
                return new ConnectionTestResultTO(false, "jdbcUrl or host+port+databaseName required for postgresql");
            }
            jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, db);
        } else {
            return new ConnectionTestResultTO(false, "unsupported database type: " + type);
        }

        try (Connection conn = "sqlite".equalsIgnoreCase(type)
                ? DriverManager.getConnection(jdbcUrl)
                : DriverManager.getConnection(jdbcUrl, to.username(), to.password())) {
            return new ConnectionTestResultTO(true, "Connection successful");
        } catch (SQLException e) {
            return new ConnectionTestResultTO(false, e.getMessage());
        }
    }

    @Override
    public void save(DatabaseConfigTO to) {
        databaseConfigRepository.save(databaseConfigMapper.toEntity(to));
    }

    @Override
    public void delete(String configId) {
        if (Global.DEFAULT_DYNAMIC_DB_KEY.equals(configId)) {
            return;
        }
        databaseConfigRepository.deleteById(configId);
    }
}
