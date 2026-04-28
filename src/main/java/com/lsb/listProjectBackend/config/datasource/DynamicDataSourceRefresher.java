package com.lsb.listProjectBackend.config.datasource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.lsb.listProjectBackend.utils.Utils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Component;

import com.lsb.listProjectBackend.entity.local.DatabaseConfig;
import com.lsb.listProjectBackend.repository.local.connection.DatabaseConfigRepository;
import com.lsb.listProjectBackend.utils.Global;

import org.sqlite.SQLiteDataSource;

/**
 * 負責將 local DB 的 {@code database_config} 記錄轉換成 DataSource，
 * 並一次性更新到 {@link DynamicRoutingDataSource} 的 routing targets 中。
 *
 * 呼叫時機：
 * - 啟動時：{@link DynamicDataSourceRegistrar} 在 ApplicationReady 事件後呼叫
 * {@link #refresh()}，確保應用一啟動就能路由到所有已設定的 DB。
 * - 設定變更時：使用者透過 API 新增、修改、刪除 database_config 後，
 * Controller 呼叫 {@link #refresh()} 使變更立即生效，無需重啟應用。
 */
@Component
public class DynamicDataSourceRefresher {

    private final DatabaseConfigRepository databaseConfigRepository;

    /** 直接持有 DynamicRoutingDataSource 型態，以呼叫其 setAllDataSources 方法。 */
    private final DynamicRoutingDataSource dynamicRoutingDataSource;

    /** 預設的 SQLite DataSource，固定作為 default target，不從 database_config 建立。 */
    private final DataSource defaultDynamicDataSource;

    public DynamicDataSourceRefresher(
            DatabaseConfigRepository databaseConfigRepository,
            @Qualifier("dynamicDataSource") DataSource dynamicDataSource,
            @Qualifier("defaultDynamicDataSource") DataSource defaultDynamicDataSource) {
        this.databaseConfigRepository = databaseConfigRepository;
        // dynamicDataSource bean 的實際型態是 DynamicRoutingDataSource，強制轉型以使用其擴充方法
        this.dynamicRoutingDataSource = (DynamicRoutingDataSource) dynamicDataSource;
        this.defaultDynamicDataSource = defaultDynamicDataSource;
    }

    /**
     * 從 local DB 讀取所有啟用的 {@code database_config}，重建 routing targets 並更新。
     *
     * 處理邏輯：
     * 1. 先把 default DataSource 放進 targets（key =
     * {@link Global#DEFAULT_DYNAMIC_DB_KEY}）
     * 2. 對每筆 enabled 的 database_config，用其 JDBC URL / driver / username / password
     * 建立一個新的 DataSource，並以 config_name 為 key 放進 targets
     * 3. configName 或 jdbcUrl 為空的記錄會略過，避免建立無效連線
     * 4. 資料庫類型為 SQLite 時，需指定 SQLiteDataSource 型態，
     * 否則 DataSourceBuilder 的自動偵測會失敗
     * 5. 呼叫 {@link DynamicRoutingDataSource#setAllDataSources} 整批替換，
     * 避免部分更新導致狀態不一致
     */
    public void refresh() {
        List<DatabaseConfig> enabledConfigs = databaseConfigRepository.findAllEnabled();
        Map<Object, Object> targets = new HashMap<>();
        // default 永遠存在，確保找不到 key 時有 fallback
        targets.put(Global.DEFAULT_DYNAMIC_DB_KEY, defaultDynamicDataSource);

        for (DatabaseConfig cfg : enabledConfigs) {
            if (cfg.getConfigId() == null || cfg.getConfigId().isBlank()) {
                continue; // configId 是 routing key，空白無法使用
            }
            if (cfg.getJdbcUrl() == null || cfg.getJdbcUrl().isBlank()) {
                continue; // 沒有 JDBC URL 無法建立連線
            }
            DataSourceBuilder<?> builder = DataSourceBuilder.create()
                    .url(cfg.getJdbcUrl())
                    .driverClassName(cfg.getDriverClassName());
            if (Utils.isNotBlank(cfg.getUsername())) {
                builder.username(cfg.getUsername());
            }
            if (Utils.isNotBlank(cfg.getPassword())) {
                builder.password(cfg.getPassword());
            }
            if (isSqlite(cfg.getDatabaseType())) {
                // SQLite 不需要帳密，但需要明確指定 DataSource 型態
                builder.type(SQLiteDataSource.class);
            }
            targets.put(cfg.getConfigId(), builder.build());
        }

        // 一次性替換所有 targets，並重設 default（找不到 key 時使用）
        dynamicRoutingDataSource.setAllDataSources(targets, defaultDynamicDataSource);
    }

    /**
     * 判斷資料庫類型是否為 SQLite（不區分大小寫）。
     */
    private static boolean isSqlite(String type) {
        return type != null && type.equalsIgnoreCase("SQLite");
    }
}
