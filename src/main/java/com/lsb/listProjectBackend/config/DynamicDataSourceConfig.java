package com.lsb.listProjectBackend.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.sqlite.SQLiteDataSource;

import com.lsb.listProjectBackend.utils.Global;
import com.lsb.listProjectBackend.utils.Utils;

import jakarta.persistence.EntityManagerFactory;

@EnableJpaRepositories(basePackages = {
        "com.lsb.listProjectBackend.repository.dynamic" }, entityManagerFactoryRef = "dynamicEntityManagerFactory", transactionManagerRef = "dynamicTransactionManager")
@Configuration
public class DynamicDataSourceConfig {

    @Bean(name = "defaultDynamicDataSource")
    public DataSource defaultDynamicDataSource() {
        // 確保資料庫已初始化（建立檔案與表格）
        DatabaseInitializer.ensureDynamicDatabaseInitialized();

        String dbPath = Utils.getDefaultFilePath(Global.DYNAMIC_SQLITE_FILE_NAME);
        return DataSourceBuilder.create()
                .url("jdbc:sqlite:" + dbPath)
                .driverClassName("org.sqlite.JDBC")
                .type(SQLiteDataSource.class)
                .build();
    }

    @Bean(name = "dynamicDataSource")
    public DataSource dynamicDataSource(
            @Qualifier("defaultDynamicDataSource") DataSource defaultDynamicDS) {
        DynamicRoutingDataSource dynamicRoutingDataSource = new DynamicRoutingDataSource();
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(Global.DEFAULT_DYNAMIC_DB_KEY, defaultDynamicDS);
        dynamicRoutingDataSource.setAllDataSources(targetDataSources, defaultDynamicDS);
        return dynamicRoutingDataSource;
    }

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

    @Bean(name = "dynamicTransactionManager")
    PlatformTransactionManager dynamicTransactionManager(
            @Qualifier("dynamicEntityManagerFactory") EntityManagerFactory dynamicEntityManagerFactory) {
        return new JpaTransactionManager(dynamicEntityManagerFactory);
    }
}
