package com.lsb.listProjectBackend.config;

import com.lsb.listProjectBackend.domain.connection.DatabaseConfigTO;
import com.lsb.listProjectBackend.utils.Global;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseInitializerMigrationTest {
    @TempDir
    Path directory;

    @Test
    void initializesExistingDatabaseAndAddsSeqWithoutLosingData() throws Exception {
        String url = "jdbc:sqlite:" + directory.resolve("old-dynamic.sqlite");
        try (var connection = DriverManager.getConnection(url);
                var statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE search_bookmark_item (
                        id TEXT NOT NULL,
                        value TEXT NOT NULL,
                        created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        PRIMARY KEY (id, value)
                    )
                    """);
            statement.execute("INSERT INTO search_bookmark_item (id, value) VALUES ('b1', 'existing')");
        }
        var config = new DatabaseConfigTO("test", "test", Global.DatabaseType.SQLITE,
                url, "org.sqlite.JDBC", null, null, null, null, null, null, null, 1, null, null, null);
        DatabaseInitializer.initializeDynamicDatabase(config);
        try (var connection = DriverManager.getConnection(url);
                var statement = connection.createStatement()) {
            try (var result = statement.executeQuery("SELECT id, value, seq FROM search_bookmark_item")) {
                assertTrue(result.next());
                assertEquals("b1", result.getString("id"));
                assertEquals("existing", result.getString("value"));
                assertNull(result.getObject("seq"));
                assertFalse(result.next());
            }
            statement.execute("UPDATE search_bookmark_item SET seq = 7 WHERE id = 'b1'");
        }
        DatabaseInitializer.initializeDynamicDatabase(config);
        try (var connection = DriverManager.getConnection(url);
                var statement = connection.createStatement();
                var result = statement.executeQuery("SELECT seq FROM search_bookmark_item WHERE id = 'b1'")) {
            assertTrue(result.next());
            assertEquals(7, result.getInt("seq"));
        }
    }

    @Test
    void migrationIsSafeForNewSchemaWithSeq() throws Exception {
        try (var connection = DriverManager.getConnection("jdbc:sqlite::memory:");
                var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE search_bookmark_item (id TEXT, value TEXT, seq INTEGER)");
            statement.execute("INSERT INTO search_bookmark_item (id, value, seq) VALUES ('b1', 'existing', 3)");
            try (var result = statement.executeQuery("SELECT seq FROM search_bookmark_item")) {
                assertTrue(result.next());
                assertEquals(3, result.getInt("seq"));
            }
        }
    }
}
