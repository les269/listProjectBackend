package com.lsb.listProjectBackend.config;

import java.util.Optional;

import com.lsb.listProjectBackend.config.datasource.DynamicDataSourceRefresher;
import com.lsb.listProjectBackend.entity.local.DatabaseConfig;
import com.lsb.listProjectBackend.repository.local.connection.DatabaseConfigRepository;
import com.lsb.listProjectBackend.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.lsb.listProjectBackend.entity.local.Setting;
import com.lsb.listProjectBackend.mapper.connection.DatabaseConfigMapper;
import com.lsb.listProjectBackend.repository.local.common.SettingRepository;
import com.lsb.listProjectBackend.utils.Global;

import lombok.extern.slf4j.Slf4j;

/**
 * 應用程式啟動後的初始化。
 * <p>
 * 注意：資料庫初始化（建立檔案與表格）已移至 {@link DatabaseInitializer}，
 * 並在 DataSource bean 建立時自動執行，確保正確的執行順序。
 */
@Slf4j
@Component
public class StartupInitializer implements ApplicationListener<ApplicationReadyEvent> {
    @Autowired
    private SettingRepository settingRepository;
    @Autowired
    private DatabaseConfigRepository databaseConfigRepository;
    @Autowired
    private DynamicDataSourceRefresher dynamicDataSourceRefresher;
    private DatabaseConfigMapper databaseConfigMapper = DatabaseConfigMapper.INSTANCE;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Optional<Setting> settingOpt = settingRepository.findById(Global.CURRENT_DYNAMIC_DB_SETTING_NAME);
        if (settingOpt.isEmpty()) {
            Setting setting = new Setting();
            setting.setName(Global.CURRENT_DYNAMIC_DB_SETTING_NAME);
            setting.setValue(Global.DEFAULT_DYNAMIC_DB_KEY);
            setting.setEnabled(true);
            settingRepository.save(setting);
        }
        Optional<DatabaseConfig> databaseConfigOptional = databaseConfigRepository
                .findById(Global.DEFAULT_DYNAMIC_DB_KEY);
        if (databaseConfigOptional.isEmpty()) {
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

        initializeCurrentDynamicDatabase();
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
            dynamicDataSourceRefresher.refresh();
            log.info("已初始化目前使用中的 dynamic database: {}", currentDatabaseKey);
        } catch (RuntimeException e) {
            log.error("初始化目前使用中的 dynamic database 失敗: {}", currentDatabaseKey, e);
        }
    }
}
