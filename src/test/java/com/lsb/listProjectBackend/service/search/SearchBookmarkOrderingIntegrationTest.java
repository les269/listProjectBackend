package com.lsb.listProjectBackend.service.search;

import com.lsb.listProjectBackend.domain.search.SearchBookmarkTO;
import com.lsb.listProjectBackend.domain.search.SearchBookmarkItemTO;
import com.lsb.listProjectBackend.domain.search.SearchBookmarkOrderTO;
import com.lsb.listProjectBackend.entity.dynamic.search.SearchBookmark;
import com.lsb.listProjectBackend.entity.dynamic.search.SearchBookmarkItem;
import com.lsb.listProjectBackend.mapper.search.SearchBookmarkMapper;
import com.lsb.listProjectBackend.repository.dynamic.search.SearchBookmarkRepository;
import com.lsb.listProjectBackend.repository.dynamic.search.SearchBookmarkItemRepository;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mapstruct.factory.Mappers;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchBookmarkOrderingIntegrationTest {
    @TempDir
    Path directory;

    @Test
    void appendsAndRenumbersOnlyTheAffectedBookmark() {
        var configuration = new Configuration()
                .addAnnotatedClass(SearchBookmark.class)
                .addAnnotatedClass(SearchBookmarkItem.class)
                .setProperty("hibernate.connection.url", "jdbc:sqlite:" + directory.resolve("ordering.sqlite"))
                .setProperty("hibernate.connection.driver_class", "org.sqlite.JDBC")
                .setProperty("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .setProperty("hibernate.connection.pool_size", "1");
        try (var factory = configuration.buildSessionFactory(); var em = factory.createEntityManager()) {
            var repositories = new JpaRepositoryFactory(em);
            var service = new SearchBookmarkServiceImpl(
                    repositories.getRepository(SearchBookmarkRepository.class),
                    repositories.getRepository(SearchBookmarkItemRepository.class),
                    Mappers.getMapper(SearchBookmarkMapper.class));
            em.getTransaction().begin();
            service.update(new SearchBookmarkTO("a", "A", null, null));
            service.update(new SearchBookmarkTO("b", "B", null, null));
            assertEquals(1, service.updateItem(request("a", "first", 99)).seq());
            assertEquals(2, service.updateItem(request("a", "second", null)).seq());
            assertEquals(3, service.updateItem(request("a", "third", null)).seq());
            assertEquals(1, service.updateItem(request("b", "other", null)).seq());
            service.updateItem(request("b", "other", 7));
            service.deleteItemById("a", "second");
            em.flush();
            em.clear();
            var remaining = service.getItemById("a");
            assertEquals(List.of("first", "third"), remaining.stream().map(SearchBookmarkItemTO::value).toList());
            assertEquals(List.of(1, 2), remaining.stream().map(SearchBookmarkItemTO::seq).toList());
            assertEquals(7, service.getItemById("b").getFirst().seq());
            assertEquals(3, service.updateItem(request("a", "fourth", null)).seq());
            service.updateItem(request("a", "fourth", 10));
            assertEquals(11, service.updateItem(request("a", "fifth", null)).seq());
            var ordered = service.updateItemOrder(new SearchBookmarkOrderTO(
                    "a", List.of("fifth", "third", "first", "fourth")));
            assertEquals(List.of(1, 2, 3, 4), ordered.stream().map(SearchBookmarkItemTO::seq).toList());
            em.flush();
            em.clear();
            assertEquals(List.of("fifth", "third", "first", "fourth"),
                    service.getItemById("a").stream().map(SearchBookmarkItemTO::value).toList());
            assertEquals(7, service.getItemById("b").getFirst().seq());
            em.getTransaction().commit();
        }
    }

    private SearchBookmarkItemTO request(String id, String value, Integer seq) {
        return new SearchBookmarkItemTO(id, value, seq, null, null);
    }
}
