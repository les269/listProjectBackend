package com.lsb.listProjectBackend.config.datasource;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.sqlite.SQLiteDataSource;

import com.lsb.listProjectBackend.config.DatabaseInitializer;
import com.lsb.listProjectBackend.utils.Global;
import com.lsb.listProjectBackend.utils.Utils;

import jakarta.persistence.EntityManagerFactory;

/**
 * Dynamic 資料庫的 Spring Bean 組裝設定。
 *
 * 整體架構：
 *
 * ┌─────────────────────────────────────────────────────────────────┐
 * │ HTTP Request │
 * │ ↓ │
 * │ Controller → Service (標有 @UseDynamic) │
 * │ ↓ ← DynamicDatabaseAspect 攔截 │
 * │ 1. 從 local.sqlite 的 setting 讀取目前選擇的 DB key │
 * │ 2. 把 key 寫入 DynamicDataSourceContextHolder (ThreadLocal) │
 * │ ↓ │
 * │ Service 執行 JPA Repository 操作 │
 * │ ↓ ← JPA 需要連線時呼叫 dynamicDataSource │
 * │ DynamicRoutingDataSource.determineCurrentLookupKey() │
 * │ ↓ ← 從 ThreadLocal 拿到 key → 回傳對應的 DataSource │
 * │ SQL 送往正確的資料庫執行 │
 * │ ↓ │
 * │ DynamicDatabaseAspect 清理 ThreadLocal │
 * └─────────────────────────────────────────────────────────────────┘
 *
 * 啟動流程：
 * 1. defaultDynamicDataSource bean 建立（確保 dynamic.sqlite 存在）
 * 2. dynamicDataSource bean 建立（建立 DynamicRoutingDataSource，初始只有 default）
 * 3. DynamicDataSourceRegistrar 在 ApplicationReady 後呼叫
 * DynamicDataSourceRefresher.refresh()，從 local DB 讀取所有啟用的
 * database_config，把每條設定對應的 DataSource 註冊進 DynamicRoutingDataSource
 * 4. 之後透過 API 新增/修改 database_config 時，再次呼叫 refresh() 即可熱更新
 */
@EnableJpaRepositories(basePackages = {
        "com.lsb.listProjectBackend.repository.dynamic" }, entityManagerFactoryRef = "dynamicEntityManagerFactory", transactionManagerRef = "dynamicTransactionManager")
@Configuration
public class DynamicDataSourceConfig {

    /**
     * 預設的 dynamic DataSource，對應 dynamic.sqlite 本地檔案。
     *
     * 此 bean 有兩個用途：
     * 1. 作為 DynamicRoutingDataSource 找不到對應 key 時的 fallback（預設資料庫）
     * 2. 以 key {@link Global#DEFAULT_DYNAMIC_DB_KEY} 直接參與 routing
     *
     * 建立前會先確保 dynamic.sqlite 檔案與表格都存在。
     */
    @Bean(name = "defaultDynamicDataSource")
    public DataSource defaultDynamicDataSource() {
        DatabaseInitializer.ensureDynamicDatabaseInitialized();

        String dbPath = Utils.getDefaultFilePath(Global.DYNAMIC_SQLITE_FILE_NAME);
        return DataSourceBuilder.create()
                .url("jdbc:sqlite:" + dbPath)
                .driverClassName("org.sqlite.JDBC")
                .type(SQLiteDataSource.class)
                .build();
    }

    /**
     * 主要的 dynamic DataSource bean，實際型態是 {@link DynamicRoutingDataSource}。
     *
     * Spring JPA 的 EntityManagerFactory 使用這個 bean。每次 JPA 需要資料庫連線時，
     * DynamicRoutingDataSource 都會呼叫 determineCurrentLookupKey() 從 ThreadLocal 取得
     * 目前 key，再從已註冊的 targets 中找到對應的 DataSource。
     *
     * 啟動時只有 default，稍後由 {@link DynamicDataSourceRefresher#refresh()} 補上其他目標。
     */
    @Bean(name = "dynamicDataSource")
    public DataSource dynamicDataSource(
            @Qualifier("defaultDynamicDataSource") DataSource defaultDynamicDS) {
        DynamicRoutingDataSource dynamicRoutingDataSource = new DynamicRoutingDataSource();
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(Global.DEFAULT_DYNAMIC_DB_KEY, defaultDynamicDS);
        dynamicRoutingDataSource.setAllDataSources(targetDataSources, defaultDynamicDS);
        return dynamicRoutingDataSource;
    }

    /**
     * Dynamic DB 的 JPA EntityManagerFactory。
     * 告知 JPA 掃描 entity.dynamic 套件下的 Entity，並使用 dynamicDataSource 作為連線來源。
     */
    @Bean(name = "dynamicEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean dynamicEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("dynamicDataSource") DataSource dynamicDataSource) {
        return builder
                .dataSource(dynamicDataSource)
                .packages("com.lsb.listProjectBackend.entity.dynamic")
                .persistenceUnit("dynamic")
                .build();
    }

    /**
     * Dynamic DB 的 JPA 交易管理器。
     * 所有標有 @Transactional 且使用 dynamic repository 的操作，
     * 都由此 TransactionManager 負責 commit / rollback。
     */
    @Bean(name = "dynamicTransactionManager")
    PlatformTransactionManager dynamicTransactionManager(
            @Qualifier("dynamicEntityManagerFactory") EntityManagerFactory dynamicEntityManagerFactory) {
        return new JpaTransactionManager(dynamicEntityManagerFactory);
    }
}
