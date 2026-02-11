package com.lsb.listProjectBackend.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.lsb.listProjectBackend.utils.Utils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Component;

import com.lsb.listProjectBackend.entity.local.DatabaseConfig;
import com.lsb.listProjectBackend.repository.local.DatabaseConfigRepository;
import com.lsb.listProjectBackend.utils.Global;

import org.sqlite.SQLiteDataSource;

/**
 * 依 local DB 的 {@code database_config} 重新註冊 dynamic 資料庫 target。
 * <p>
 * 啟動時由 {@link DynamicDataSourceRegistrar} 呼叫；{@code database_config} 變動後
 * 可呼叫 {@link #refresh()}（例如透過 API）以更新 routing targets。
 */
@Component
public class DynamicDataSourceRefresher {

    private final DatabaseConfigRepository databaseConfigRepository;
    private final DynamicRoutingDataSource dynamicRoutingDataSource;
    private final DataSource defaultDynamicDataSource;

    public DynamicDataSourceRefresher(
            DatabaseConfigRepository databaseConfigRepository,
            @Qualifier("dynamicDataSource") DataSource dynamicDataSource,
            @Qualifier("defaultDynamicDataSource") DataSource defaultDynamicDataSource) {
        this.databaseConfigRepository = databaseConfigRepository;
        this.dynamicRoutingDataSource = (DynamicRoutingDataSource) dynamicDataSource;
        this.defaultDynamicDataSource = defaultDynamicDataSource;
    }

    /**
     * 從 local DB 讀取 enabled 的 {@code database_config}，重建 dynamic routing targets
     * （含 default），並呼叫 {@link DynamicRoutingDataSource#setAllDataSources}。
     */
    public void refresh() {
        List<DatabaseConfig> enabledConfigs = databaseConfigRepository.findAllEnabled();
        Map<Object, Object> targets = new HashMap<>();
        targets.put(Global.DEFAULT_DYNAMIC_DB_KEY, defaultDynamicDataSource);

        for (DatabaseConfig cfg : enabledConfigs) {
            if (cfg.getConfigName() == null || cfg.getConfigName().isBlank()) {
                continue;
            }
            if (cfg.getJdbcUrl() == null || cfg.getJdbcUrl().isBlank()) {
                continue;
            }
            String key = cfg.getConfigName();
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
                builder.type(SQLiteDataSource.class);
            }
            targets.put(key, builder.build());
        }

        dynamicRoutingDataSource.setAllDataSources(targets, defaultDynamicDataSource);
    }

    private static boolean isSqlite(String type) {
        return type != null && type.equalsIgnoreCase("SQLite");
    }
}
