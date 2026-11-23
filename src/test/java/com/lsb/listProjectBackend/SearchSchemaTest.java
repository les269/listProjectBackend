package com.lsb.listProjectBackend;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class SearchSchemaTest {
    @Test
    void supportsSharedIdsAndValueLookupInBothSchemas() throws Exception {
        for (String resource : new String[]{"/dynamic.sql", "/dynamic-postgresql.sql"}) {
            try (var connection = DriverManager.getConnection("jdbc:sqlite::memory:");
                 var statement = connection.createStatement();
                 var stream = getClass().getResourceAsStream(resource)) {
                assertNotNull(stream);
                statement.execute("PRAGMA foreign_keys = ON");
                String sql = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                // Re-running initialization must preserve existing rows.
                for (int run = 0; run < 2; run++) {
                    for (String ddl : sql.split(";")) {
                        if (ddl.contains("CREATE TABLE IF NOT EXISTS search_")
                                || ddl.contains("create TABLE IF NOT EXISTS search_")) {
                            statement.execute(ddl);
                        }
                    }
                    if (run == 0) {
                        statement.execute("INSERT INTO search_bookmark (id, name) VALUES ('s1', '名稱')");
                        statement.execute("INSERT INTO search_bookmark_item (id, value, seq) VALUES ('s1', '搜尋值', 2)");
                        statement.execute("INSERT INTO search_bookmark_item (id, value) VALUES ('s1', '第二個搜尋值')");
                    }
                }
                for (String table : new String[]{"search_bookmark", "search_bookmark_item"}) {
                    try (var result = statement.executeQuery(
                            "SELECT created_time, updated_time FROM " + table + " WHERE id = 's1'")) {
                        assertTrue(result.next());
                        assertNotNull(result.getTimestamp("created_time"));
                        assertNotNull(result.getTimestamp("updated_time"));
                    }
                    assertThrows(SQLException.class, () -> statement.execute(
                            "UPDATE " + table + " SET created_time = NULL WHERE id = 's1'"));
                    assertThrows(SQLException.class, () -> statement.execute(
                            "UPDATE " + table + " SET updated_time = NULL WHERE id = 's1'"));
                }
                try (var result = statement.executeQuery(
                        "SELECT seq FROM search_bookmark_item WHERE id = 's1' AND value = '搜尋值'")) {
                    assertTrue(result.next());
                    assertEquals(2, result.getInt("seq"));
                }
                try (var result = statement.executeQuery(
                        "SELECT seq FROM search_bookmark_item WHERE id = 's1' AND value = '第二個搜尋值'")) {
                    assertTrue(result.next());
                    assertNull(result.getObject("seq"));
                }
                try (var query = connection.prepareStatement("""
                        SELECT n.id, n.name FROM search_bookmark n
                        JOIN search_bookmark_item v ON v.id = n.id WHERE v.value = ?
                        """)) {
                    query.setString(1, "搜尋值");
                    try (var result = query.executeQuery()) {
                        assertTrue(result.next());
                        assertEquals("s1", result.getString("id"));
                        assertEquals("名稱", result.getString("name"));
                        assertFalse(result.next());
                    }
                }
                assertThrows(SQLException.class, () -> statement.execute(
                        "INSERT INTO search_bookmark_item (id, value) VALUES ('missing', 'orphan')"));
                assertThrows(SQLException.class, () -> statement.execute(
                        "INSERT INTO search_bookmark_item (id, value) VALUES ('s1', '搜尋值')"));
                assertThrows(SQLException.class, () -> statement.execute(
                        "INSERT INTO search_bookmark_item (id, value) VALUES ('s1', NULL)"));
                try (var result = statement.executeQuery(
                        "SELECT COUNT(*) FROM search_bookmark_item WHERE id = 's1'")) {
                    assertTrue(result.next());
                    assertEquals(2, result.getInt(1));
                }
            }
        }
    }
}
