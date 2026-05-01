package com.lsb.listProjectBackend.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.stream.Collectors;

import com.lsb.listProjectBackend.utils.Utils;
import org.springframework.core.io.ClassPathResource;

import com.lsb.listProjectBackend.domain.connection.DatabaseConfigTO;
import com.lsb.listProjectBackend.utils.Global;

import lombok.extern.slf4j.Slf4j;

/**
 * 資料庫初始化工具。確保資料庫檔案與表格在 DataSource bean 建立前就存在。
 *
 * 所有 public 方法均為靜態，供 LocalDataSourceConfig / DynamicDataSourceConfig
 * 在 @Bean 方法內直接呼叫，不需要注入此類別。
 *
 * 冪等設計：ensureLocalDatabaseInitialized / ensureDynamicDatabaseInitialized
 * 透過 volatile flag 保證同一 JVM 生命週期內只執行一次，多執行緒安全。
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

    /** 確保資料目錄存在，不存在時自動建立，並回傳目錄路徑。 */
    private static Path ensureDataFolderExists() throws IOException {
        Path dataFolderPath = Utils.getDefaultDirectoryPath();

        if (!Files.exists(dataFolderPath)) {
            Files.createDirectories(dataFolderPath);
            log.info("已建立資料夾: {}", dataFolderPath.toAbsolutePath());
        }
        return dataFolderPath;
    }

    /**
     * 根據 DatabaseConfigTO 的資料庫類型，初始化對應的 dynamic 資料庫表格結構。
     * 支援 SQLite 與 PostgreSQL；其他類型僅記錄警告，不拋出例外。
     * 此方法在每次切換 dynamic DB 時由 DatabaseStartupInitializer 呼叫，不具冪等 flag，
     * 因此 SQL 使用 CREATE TABLE IF NOT EXISTS，重複執行不會有副作用。
     *
     * @param databaseConfigTO 目標資料庫的連線設定
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

    /** 對 SQLite 資料庫執行 dynamic.sql，建立所需表格（SQLite 會自動建立檔案）。 */
    private static void initializeSqliteDatabase(String jdbcUrl) throws SQLException, IOException {
        String sql = loadSql(Global.DYNAMIC_SQL_FILE_NAME);
        try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
            executeSqlStatements(connection, sql);
        }
        log.info("已成功執行 dynamic.sql 建立 SQLite 資料庫表格");
    }

    /** 對 PostgreSQL 資料庫執行 dynamic-postgresql.sql，建立所需表格。帳密為空時不帶入 Properties。 */
    private static void initializePostgresqlDatabase(
            String jdbcUrl,
            String username,
            String password) throws SQLException, IOException {
        String sql = loadSql(Global.DYNAMIC_POSTGRESQL_SQL_FILE_NAME);
        Properties props = new Properties();
        if (Utils.isNotBlank(username)) {
            props.setProperty("user", username);
        }
        if (Utils.isNotBlank(password)) {
            props.setProperty("password", password);
        }
        try (Connection connection = DriverManager.getConnection(jdbcUrl, props)) {
            executeSqlStatements(connection, sql);
        }
        log.info("已成功執行 dynamic-postgresql.sql 建立 PostgreSQL 資料庫表格");
    }

    /**
     * 在指定目錄下初始化 SQLite 資料庫，並執行對應 SQL 文件建立表格。
     * 供 ensureLocalDatabaseInitialized / ensureDynamicDatabaseInitialized 呼叫。
     */
    private static void initializeDatabase(Path dataFolderPath, String dbFileName, String sqlFileName)
            throws SQLException, IOException {
        Path dbFilePath = dataFolderPath.resolve(dbFileName);
        String jdbcUrl = "jdbc:sqlite:" + dbFilePath.toAbsolutePath();
        String sql = loadSql(sqlFileName);
        try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
            executeSqlStatements(connection, sql);
        }
        log.info("已成功執行 {} 建立資料庫表格", sqlFileName);
    }

    /** 從 classpath 讀取 SQL 文件，回傳完整 SQL 字串。 */
    private static String loadSql(String sqlFileName) throws IOException {
        ClassPathResource resource = new ClassPathResource(sqlFileName);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    /** 將 SQL 字串依 `;` 分割後逐一執行，跳過空白語句。 */
    private static void executeSqlStatements(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            for (String stmt : sql.split(";")) {
                stmt = stmt.trim();
                if (!stmt.isEmpty()) {
                    statement.execute(stmt);
                }
            }
        }
    }
}
