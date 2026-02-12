package com.lsb.listProjectBackend.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.sqlite.SQLiteDataSource;

import com.lsb.listProjectBackend.utils.Global;
import com.lsb.listProjectBackend.utils.Utils;

import jakarta.persistence.EntityManagerFactory;

@EnableJpaRepositories(basePackages = {
        "com.lsb.listProjectBackend.repository.local"}, entityManagerFactoryRef = "localEntityManagerFactory", transactionManagerRef = "localTransactionManager")
@Configuration
public class LocalDataSourceConfig {

    @Primary
    @Bean(name = "localDataSource")
    public DataSource localDataSource() {
        // 確保資料庫已初始化（建立檔案與表格）
        DatabaseInitializer.ensureLocalDatabaseInitialized();

        String dbPath = Utils.getDefaultFilePath(Global.LOCAL_SQLITE_FILE_NAME);
        return DataSourceBuilder.create()
                .url("jdbc:sqlite:"
                        + dbPath)
                .driverClassName("org.sqlite.JDBC")
                .type(SQLiteDataSource.class)
                .build();
    }

    @Primary
    @Bean(name = "localEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean localEntityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                            @Qualifier("localDataSource") DataSource localDataSource) {
        return builder
                .dataSource(
                        localDataSource)
                .packages("com.lsb.listProjectBackend.entity.local")
                .persistenceUnit("local")
                .build();
    }

    @Primary
    @Bean(name = "localTransactionManager")
    PlatformTransactionManager localTransactionManager(
            @Qualifier("localEntityManagerFactory") EntityManagerFactory localEntityManagerFactory) {
        return new JpaTransactionManager(localEntityManagerFactory);
    }
}
