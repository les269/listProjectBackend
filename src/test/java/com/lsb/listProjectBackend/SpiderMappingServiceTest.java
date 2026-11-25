package com.lsb.listProjectBackend;

import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderMapping;
import com.lsb.listProjectBackend.mapper.spider.SpiderMappingMapper;
import com.lsb.listProjectBackend.repository.dynamic.spider.SpiderMappingRepository;
import com.lsb.listProjectBackend.service.spider.SpiderMappingService;
import com.lsb.listProjectBackend.service.spider.SpiderMappingServiceImpl;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SpiderMappingServiceTest {
    private AnnotationConfigApplicationContext context;
    private JdbcTemplate jdbc;
    private SpiderMappingService service;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext(TestConfig.class);
        jdbc = new JdbcTemplate(context.getBean(SingleConnectionDataSource.class));
        jdbc.execute("CREATE TABLE spider_mapping (spider_id TEXT NOT NULL, execution_order INTEGER NOT NULL, "
                + "spider_item_id TEXT NOT NULL, updated_time TIMESTAMP NOT NULL, "
                + "PRIMARY KEY (spider_id, execution_order))");
        jdbc.execute("INSERT INTO spider_mapping VALUES "
                + "('spider', 0, 'a', CURRENT_TIMESTAMP), ('spider', 1, 'b', CURRENT_TIMESTAMP), "
                + "('spider', 2, 'c', CURRENT_TIMESTAMP), ('other', 7, 'a', CURRENT_TIMESTAMP)");
        service = context.getBean(SpiderMappingService.class);
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void deletingAnyPositionRenumbersRemainingItemsAndKeepsOtherSpiders(int order) {
        List<String> items = List.of("a", "b", "c");
        service.delete("spider", order, items.get(order));

        assertEquals(List.of(0, 1), orders("spider"));
        assertEquals(IntStream.range(0, items.size()).filter(i -> i != order)
                .mapToObj(items::get).toList(), items("spider"));
        assertEquals(List.of(7), orders("other"));
        assertEquals(List.of("a"), items("other"));
    }

    @Test
    void missingOrMismatchedTargetLeavesMappingsUnchanged() {
        service.delete("spider", 1, "wrong-item");
        service.delete("spider", 9, "b");
        service.delete("missing", 0, "a");

        assertEquals(List.of(0, 1, 2), orders("spider"));
        assertEquals(List.of("a", "b", "c"), items("spider"));
    }

    @Test
    void deletingOnlyItemLeavesSpiderEmpty() {
        service.delete("other", 7, "a");

        assertEquals(List.of(), orders("other"));
        assertEquals(List.of(0, 1, 2), orders("spider"));
    }

    @Test
    void deletionNormalizesExistingGapsAndPreservesRepeatedItems() {
        jdbc.execute("UPDATE spider_mapping SET execution_order = 8, spider_item_id = 'a' "
                + "WHERE spider_id = 'spider' AND execution_order = 2");

        service.delete("spider", 1, "b");

        assertEquals(List.of(0, 1), orders("spider"));
        assertEquals(List.of("a", "a"), items("spider"));
    }

    @Test
    void failedReinsertRollsBackDeletion() {
        jdbc.execute("CREATE TRIGGER reject_reinsert BEFORE INSERT ON spider_mapping "
                + "WHEN NEW.spider_id = 'spider' BEGIN SELECT RAISE(ABORT, 'reject reinsert'); END");

        assertThrows(RuntimeException.class, () -> service.delete("spider", 1, "b"));

        assertEquals(List.of(0, 1, 2), orders("spider"));
        assertEquals(List.of("a", "b", "c"), items("spider"));
    }

    private List<Integer> orders(String spiderId) {
        return jdbc.queryForList("SELECT execution_order FROM spider_mapping "
                + "WHERE spider_id = ? ORDER BY execution_order", Integer.class, spiderId);
    }

    private List<String> items(String spiderId) {
        return jdbc.queryForList("SELECT spider_item_id FROM spider_mapping "
                + "WHERE spider_id = ? ORDER BY execution_order", String.class, spiderId);
    }

    @Configuration
    @EnableTransactionManagement
    static class TestConfig {
        @Bean
        SingleConnectionDataSource dataSource() {
            return new SingleConnectionDataSource("jdbc:sqlite::memory:", true);
        }

        @Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory(SingleConnectionDataSource dataSource) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setManagedTypes(PersistenceManagedTypes.of(SpiderMapping.class.getName()));
            factory.setJpaPropertyMap(Map.of("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect"));
            return factory;
        }

        @Bean
        JpaTransactionManager dynamicTransactionManager(EntityManagerFactory factory) {
            return new JpaTransactionManager(factory);
        }

        @Bean
        SpiderMappingService service(EntityManagerFactory factory) {
            var repository = new JpaRepositoryFactory(SharedEntityManagerCreator.createSharedEntityManager(factory))
                    .getRepository(SpiderMappingRepository.class);
            return new SpiderMappingServiceImpl(repository, Mappers.getMapper(SpiderMappingMapper.class));
        }
    }
}
