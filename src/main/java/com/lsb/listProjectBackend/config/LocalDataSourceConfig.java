package com.lsb.listProjectBackend.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
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

/**
 * Local 資料庫的 Spring Bean 組裝設定。
 * 組裝 localDataSource → localEntityManagerFactory → localTransactionManager，
 * 供 repository.local 套件下的所有 JPA Repository 使用。
 * 所有 bean 標為 @Primary，作為 Spring 預設選用的 JPA 基礎建設。
 */
@EnableJpaRepositories(basePackages = {
                "com.lsb.listProjectBackend.repository.local" }, entityManagerFactoryRef = "localEntityManagerFactory", transactionManagerRef = "localTransactionManager")
@Configuration
public class LocalDataSourceConfig {

        /**
         * Local 資料庫（local.sqlite）的 DataSource bean，標為 @Primary 作為預設連線。
         * 建立前先確保 local.sqlite 檔案與表格已存在。
         */
        @Primary
        @Bean(name = "localDataSource")
        public DataSource localDataSource() {
                DatabaseInitializer.ensureLocalDatabaseInitialized();

                String dbPath = Utils.getDefaultFilePath(Global.LOCAL_SQLITE_FILE_NAME);
                return DataSourceBuilder.create().url("jdbc:sqlite:" + dbPath).driverClassName("org.sqlite.JDBC")
                                .type(SQLiteDataSource.class)
                                .build();
        }

        /**
         * Local DB 的 JPA EntityManagerFactory。
         * 掃描 entity.local 套件下的 Entity，並使用 localDataSource 作為連線來源。
         */
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

        /**
         * Local DB 的 JPA 交易管理器。
         * 所有標有 @Transactional 且使用 local repository 的操作，都由此 TransactionManager 負責 commit
         * / rollback。
         */
        @Primary
        @Bean(name = "localTransactionManager")
        PlatformTransactionManager localTransactionManager(
                        @Qualifier("localEntityManagerFactory") EntityManagerFactory localEntityManagerFactory) {
                return new JpaTransactionManager(localEntityManagerFactory);
        }
}
