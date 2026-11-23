package com.lsb.listProjectBackend.config.datasource;

import com.lsb.listProjectBackend.aop.UseDynamicTx;
import com.lsb.listProjectBackend.entity.local.Setting;
import com.lsb.listProjectBackend.repository.local.common.SettingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DynamicDatabaseAspectTest {
    @Test
    void setsSelectedDatabaseBeforeTransactionAndClearsAfterCommit() {
        try (var context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            var service = context.getBean(TransactionalService.class);
            assertEquals("origin", service.currentDatabase(false));
            var transactions = context.getBean(RecordingTransactionManager.class);
            assertEquals("origin", transactions.keyAtBegin);
            assertEquals("origin", transactions.keyAtCompletion);
            assertNull(DynamicDataSourceContextHolder.getDataSourceKey());
        } finally {
            DynamicDataSourceContextHolder.clear();
        }
    }

    @Test
    void retainsSelectedDatabaseThroughRollbackAndClearsAfterFailure() {
        try (var context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            var service = context.getBean(TransactionalService.class);
            assertThrows(IllegalStateException.class, () -> service.currentDatabase(true));
            var transactions = context.getBean(RecordingTransactionManager.class);
            assertEquals("origin", transactions.keyAtBegin);
            assertEquals("origin", transactions.keyAtCompletion);
            assertNull(DynamicDataSourceContextHolder.getDataSourceKey());
        } finally {
            DynamicDataSourceContextHolder.clear();
        }
    }

    @UseDynamicTx
    public static class TransactionalService {
        public String currentDatabase(boolean fail) {
            if (fail) {
                throw new IllegalStateException("Rollback test");
            }
            return DynamicDataSourceContextHolder.getDataSourceKey();
        }
    }

    @Configuration
    @EnableAspectJAutoProxy
    @EnableTransactionManagement
    static class TestConfig {
        @Bean
        SettingRepository settings() {
            var repository = mock(SettingRepository.class);
            var setting = new Setting();
            setting.setValue("origin");
            when(repository.findById(anyString())).thenReturn(Optional.of(setting));
            return repository;
        }

        @Bean
        DynamicDatabaseAspect aspect(SettingRepository settings) {
            return new DynamicDatabaseAspect(settings);
        }

        @Bean(name = "dynamicTransactionManager")
        RecordingTransactionManager transactions() {
            return new RecordingTransactionManager();
        }

        @Bean
        TransactionalService service() {
            return new TransactionalService();
        }
    }

    static class RecordingTransactionManager extends AbstractPlatformTransactionManager {
        String keyAtBegin;
        String keyAtCompletion;

        @Override
        protected Object doGetTransaction() {
            return new Object();
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {
            keyAtBegin = DynamicDataSourceContextHolder.getDataSourceKey();
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
            keyAtCompletion = DynamicDataSourceContextHolder.getDataSourceKey();
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) {
            keyAtCompletion = DynamicDataSourceContextHolder.getDataSourceKey();
        }
    }
}
