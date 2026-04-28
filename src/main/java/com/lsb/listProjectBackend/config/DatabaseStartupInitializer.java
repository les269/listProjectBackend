package com.lsb.listProjectBackend.config;

import java.util.Optional;

import com.lsb.listProjectBackend.config.datasource.DynamicDataSourceRefresher;
import com.lsb.listProjectBackend.entity.local.DatabaseConfig;
import com.lsb.listProjectBackend.repository.local.connection.DatabaseConfigRepository;
import com.lsb.listProjectBackend.utils.Utils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.lsb.listProjectBackend.entity.local.Setting;
import com.lsb.listProjectBackend.mapper.connection.DatabaseConfigMapper;
import com.lsb.listProjectBackend.repository.local.common.SettingRepository;
import com.lsb.listProjectBackend.utils.Global;

import lombok.extern.slf4j.Slf4j;

/**
 * 應用程式啟動後的資料庫初始化。
 * 確保預設 setting、database_config 記錄存在，並初始化目前選用的 dynamic database。
 *
 * 注意：SQLite 檔案與表格的建立已移至 {@link DatabaseInitializer}，
 * 在 DataSource bean 建立時自動執行，確保正確的執行順序。
 */
@Slf4j
@Component
public class DatabaseStartupInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final SettingRepository settingRepository;
    private final DatabaseConfigRepository databaseConfigRepository;
    private final DynamicDataSourceRefresher dynamicDataSourceRefresher;
    private final DatabaseConfigMapper databaseConfigMapper = DatabaseConfigMapper.INSTANCE;

    public DatabaseStartupInitializer(SettingRepository settingRepository,
            DatabaseConfigRepository databaseConfigRepository,
            DynamicDataSourceRefresher dynamicDataSourceRefresher) {
        this.settingRepository = settingRepository;
        this.databaseConfigRepository = databaseConfigRepository;
        this.dynamicDataSourceRefresher = dynamicDataSourceRefresher;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ensureDefaultSettingExists();
        ensureDefaultDatabaseConfigExists();
        initializeCurrentDynamicDatabase();
    }

    private void ensureDefaultSettingExists() {
        if (settingRepository.findById(Global.CURRENT_DYNAMIC_DB_SETTING_NAME).isEmpty()) {
            Setting setting = new Setting();
            setting.setName(Global.CURRENT_DYNAMIC_DB_SETTING_NAME);
            setting.setValue(Global.DEFAULT_DYNAMIC_DB_KEY);
            setting.setEnabled(true);
            settingRepository.save(setting);
        }
    }

    private void ensureDefaultDatabaseConfigExists() {
        if (databaseConfigRepository.findById(Global.DEFAULT_DYNAMIC_DB_KEY).isPresent()) {
            return;
        }
        DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.setConfigId(Global.DEFAULT_DYNAMIC_DB_KEY);
        databaseConfig.setConfigName(Global.DEFAULT_DYNAMIC_DB_KEY);
        databaseConfig.setDatabaseType(Global.SQLITE_TYPE);
        databaseConfig.setJdbcUrl("jdbc:sqlite:" + Utils.getDefaultFilePath(Global.DYNAMIC_SQLITE_FILE_NAME));
        databaseConfig.setDriverClassName("org.sqlite.JDBC");
        databaseConfig.setEnabled(1);
        databaseConfig.setDescription("");
        databaseConfig.setAdditionalProperties("");
        databaseConfig.setHost("localhost");
        databaseConfig.setPort(5432);
        databaseConfig.setDatabaseName("");
        databaseConfig.setUsername("");
        databaseConfig.setPassword("");
        databaseConfigRepository.save(databaseConfig);
    }

    private void initializeCurrentDynamicDatabase() {
        String currentDatabaseKey = settingRepository.findById(Global.CURRENT_DYNAMIC_DB_SETTING_NAME)
                .map(Setting::getValue)
                .filter(value -> value != null && !value.isBlank())
                .orElse(Global.DEFAULT_DYNAMIC_DB_KEY);

        Optional<DatabaseConfig> currentDatabaseConfig = databaseConfigRepository.findByConfigName(currentDatabaseKey);
        if (currentDatabaseConfig.isEmpty()) {
            currentDatabaseConfig = databaseConfigRepository.findById(currentDatabaseKey);
        }

        if (currentDatabaseConfig.isEmpty()) {
            log.warn("找不到目前設定的 dynamic database: {}", currentDatabaseKey);
            dynamicDataSourceRefresher.refresh();
            return;
        }

        try {
            DatabaseConfig databaseConfig = currentDatabaseConfig.get();
            DatabaseInitializer.initializeDynamicDatabase(databaseConfigMapper.toDomain(databaseConfig));
            log.info("已初始化目前使用中的 dynamic database: {}", currentDatabaseKey);
        } catch (RuntimeException e) {
            log.error("初始化目前使用中的 dynamic database 失敗: {}", currentDatabaseKey, e);
        } finally {
            dynamicDataSourceRefresher.refresh();
        }
    }
}
