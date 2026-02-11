package com.lsb.listProjectBackend.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

import com.lsb.listProjectBackend.utils.Utils;
import org.hibernate.dialect.PostgreSQLDialect;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.core.io.ClassPathResource;

import com.lsb.listProjectBackend.domain.DatabaseConfigTO;
import com.lsb.listProjectBackend.utils.Global;

import lombok.extern.slf4j.Slf4j;
import org.sqlite.SQLiteDataSource;

/**
 * 資料庫初始化工具。確保 SQLite 檔案與表格在 DataSource 建立前就存在。
 */
@Slf4j
public class DatabaseInitializer {

    private static volatile boolean localInitialized = false;
    private static volatile boolean dynamicInitialized = false;

    /**
     * 確保 local 資料庫（local.sqlite）已初始化。
     * 會建立資料夾、檔案，並執行 local.sql 建立表格。
     */
    public static synchronized void ensureLocalDatabaseInitialized() {
        if (localInitialized) {
            return;
        }
        try {
            Path dataFolderPath = ensureDataFolderExists();
            initializeDatabase(dataFolderPath, Global.LOCAL_SQLITE_FILE_NAME, Global.LOCAL_SQL_FILE_NAME);
            localInitialized = true;
        } catch (IOException | SQLException e) {
            log.error("初始化 local 資料庫失敗: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize local database", e);
        }
    }

    /**
     * 確保 dynamic 資料庫（dynamic.sqlite）已初始化。
     * 會建立資料夾、檔案，並執行 dynamic.sql 建立表格。
     */
    public static synchronized void ensureDynamicDatabaseInitialized() {
        if (dynamicInitialized) {
            return;
        }
        try {
            Path dataFolderPath = ensureDataFolderExists();
            initializeDatabase(dataFolderPath, Global.DYNAMIC_SQLITE_FILE_NAME, Global.DYNAMIC_SQL_FILE_NAME);
            dynamicInitialized = true;
        } catch (IOException | SQLException e) {
            log.error("初始化 dynamic 資料庫失敗: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize dynamic database", e);
        }
    }

    private static Path ensureDataFolderExists() throws IOException {
        Path dataFolderPath = Utils.getDefaultDirectoryPath();

        if (!Files.exists(dataFolderPath)) {
            Files.createDirectories(dataFolderPath);
            log.info("已建立資料夾: {}", dataFolderPath.toAbsolutePath());
        }
        return dataFolderPath;
    }

    /**
     * 根據資料庫類型初始化 dynamic 資料庫（支援 SQLite 和 PostgreSQL）
     *
     */
    public static synchronized void initializeDynamicDatabase(
            DatabaseConfigTO databaseConfigTO) {
        String databaseType = databaseConfigTO.getDatabaseType();
        try {
            if ("sqlite".equalsIgnoreCase(databaseType)) {
                initializeSqliteDatabase(databaseConfigTO.getJdbcUrl());
            } else if ("postgresql".equalsIgnoreCase(databaseType)) {
                initializePostgresqlDatabase(
                        databaseConfigTO.getJdbcUrl(),
                        databaseConfigTO.getUsername(),
                        databaseConfigTO.getPassword());
            } else {
                log.warn("Unsupported database type: {}", databaseType);
            }
        } catch (SQLException | IOException e) {
            log.error("初始化 dynamic 資料庫失敗 ({}): {}", databaseType, e.getMessage(), e);
            throw new RuntimeException("Failed to initialize dynamic database: " + databaseType, e);
        }
    }

    private static void initializeSqliteDatabase(String jdbcUrl) throws SQLException, IOException {
        // 讀取 dynamic.sql 檔案
        ClassPathResource resource = new ClassPathResource(Global.DYNAMIC_SQL_FILE_NAME);
        String sql;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream()))) {
            sql = reader.lines().collect(Collectors.joining("\n"));
        }

        // 連接到資料庫並執行 SQL（SQLite 會自動建立檔案）
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement()) {

            String[] sqlStatements = sql.split(";");
            for (String sqlStatement : sqlStatements) {
                sqlStatement = sqlStatement.trim();
                if (!sqlStatement.isEmpty()) {
                    statement.execute(sqlStatement);
                }
            }

            log.info("已成功執行 dynamic.sql 建立 SQLite 資料庫表格");
        }
    }

    private static void initializePostgresqlDatabase(
            String jdbcUrl,
            String username,
            String password) throws SQLException, IOException {
        // 讀取 dynamic.sql 檔案
        ClassPathResource resource = new ClassPathResource(Global.DYNAMIC_SQL_FILE_NAME);
        String sql;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream()))) {
            sql = reader.lines().collect(Collectors.joining("\n"));
        }

        java.util.Properties props = new java.util.Properties();
        if (Utils.isNotBlank(username)) {
            props.setProperty("user", username);
        }
        if (Utils.isNotBlank(password)) {
            props.setProperty("password", password);
        }

        try (Connection connection = DriverManager.getConnection(jdbcUrl, props);
             Statement statement = connection.createStatement()) {

            String[] sqlStatements = sql.split(";");
            for (String sqlStatement : sqlStatements) {
                sqlStatement = sqlStatement.trim();
                if (!sqlStatement.isEmpty()) {
                    statement.execute(sqlStatement);
                }
            }

            log.info("已成功執行 dynamic.sql 建立 PostgreSQL 資料庫表格");
        }
    }

    private static void initializeDatabase(Path dataFolderPath, String dbFileName, String sqlFileName)
            throws SQLException, IOException {
        Path dbFilePath = dataFolderPath.resolve(dbFileName);
        String jdbcUrl = "jdbc:sqlite:" + dbFilePath.toAbsolutePath();

        // 讀取 SQL 檔案
        ClassPathResource resource = new ClassPathResource(sqlFileName);
        String sql;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream()))) {
            sql = reader.lines().collect(Collectors.joining("\n"));
        }

        // 連接到資料庫並執行 SQL（SQLite 會自動建立檔案）
        try (Connection connection = DataSourceBuilder.create()
                .url(jdbcUrl)
                .driverClassName("org.sqlite.JDBC")
                .type(SQLiteDataSource.class)
                .build()
                .getConnection();
             Statement statement = connection.createStatement()) {

            String[] sqlStatements = sql.split(";");
            for (String sqlStatement : sqlStatements) {
                sqlStatement = sqlStatement.trim();
                if (!sqlStatement.isEmpty()) {
                    statement.execute(sqlStatement);
                }
            }

            log.info("已成功執行 {} 建立資料庫表格", sqlFileName);
        }
    }
}
