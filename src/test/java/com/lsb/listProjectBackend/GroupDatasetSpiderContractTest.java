package com.lsb.listProjectBackend;

import com.lsb.listProjectBackend.entity.dynamic.dataset.GroupDatasetConfig;
import com.lsb.listProjectBackend.entity.dynamic.dataset.GroupDataset;
import com.lsb.listProjectBackend.utils.Global;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

class GroupDatasetSpiderContractTest {
    private final ObjectMapper json = new ObjectMapper();

    @Test
    void readsAndWritesSpiderReferencesIncludingDefaultFlag() {
        var config = json.readValue("""
                {"type":"spider","byKey":"id","groupDatasetSpiderList":[
                  {"seq":1,"spiderId":"detail","label":"Details","isDefault":true,
                   "visibleJson":true,"visibleUrl":false}]}
                """, GroupDatasetConfig.class);

        assertEquals(Global.GroupDatasetConfigType.spider, config.getType());
        var spider = config.getGroupDatasetSpiderList().getFirst();
        assertEquals("detail", spider.getSpiderId());
        assertTrue(spider.isDefault());
        assertTrue(spider.isVisibleJson());
        var stored = json.readTree(json.writeValueAsString(config));
        assertTrue(stored.at("/groupDatasetSpiderList/0/isDefault").asBoolean());
        assertEquals("detail", stored.at("/groupDatasetSpiderList/0/spiderId").asText());
    }

    @Test
    void rejectsOldScrapyTypeAfterSwitchToSpider() {
        assertThrows(IllegalArgumentException.class,
                () -> Global.GroupDatasetConfigType.valueOf("scrapy"));
    }

    @Test
    void persistenceJsonRoundTripPreservesSpiderSettings() throws Exception {
        // Hibernate's JSON mapper and Spring MVC must agree on the persisted contract.
        var persistence = new com.fasterxml.jackson.databind.ObjectMapper();
        var group = persistence.readValue("""
                {"groupName":"group","config":{"type":"spider","groupDatasetSpiderList":[
                  {"spiderId":"detail","isDefault":true}]}}
                """, GroupDataset.class);
        var response = json.readTree(persistence.writeValueAsString(group));
        assertEquals("spider", response.at("/config/type").asText());
        assertTrue(response.at("/config/groupDatasetSpiderList/0/isDefault").asBoolean());
    }

    @Test
    void sqliteMigrationClearsOldReferencesAndPreservesOtherSettingsWhenRepeated() throws Exception {
        String migration = java.nio.file.Files.readString(java.nio.file.Path.of(
                "docs/migrations/group-dataset-spider-sqlite.sql"));
        try (var connection = java.sql.DriverManager.getConnection("jdbc:sqlite::memory:");
                var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE group_dataset (group_name TEXT PRIMARY KEY, config TEXT NOT NULL)");
            statement.execute("""
                    INSERT INTO group_dataset VALUES
                    ('old', '{"type":"scrapy","byKey":"id","imageSaveFolder":"images",
                              "groupDatasetScrapyList":[{"name":"legacy"}]}'),
                    ('api', '{"type":"api","groupDatasetApiList":[{"apiId":"a"}],
                              "groupDatasetScrapyList":[]}'),
                    ('new', '{"type":"spider","groupDatasetSpiderList":[{"spiderId":"detail"}]}')
                    """);
            statement.executeUpdate(migration);
            statement.executeUpdate(migration);
            try (var result = statement.executeQuery("SELECT config FROM group_dataset WHERE group_name = 'old'")) {
                assertTrue(result.next());
                String stored = result.getString(1);
                var config = json.readValue(stored, GroupDatasetConfig.class);
                assertEquals(Global.GroupDatasetConfigType.spider, config.getType());
                assertTrue(config.getGroupDatasetSpiderList().isEmpty());
                assertEquals("id", config.getByKey());
                assertEquals("images", config.getImageSaveFolder());
                assertFalse(json.readTree(stored).has("groupDatasetScrapyList"));
                // The persistence mapper must also accept migrated JSON.
                assertNotNull(new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(stored, GroupDatasetConfig.class));
            }
            try (var result = statement.executeQuery("SELECT config FROM group_dataset WHERE group_name = 'api'")) {
                assertTrue(result.next());
                var config = json.readValue(result.getString(1), GroupDatasetConfig.class);
                assertEquals(Global.GroupDatasetConfigType.api, config.getType());
                assertEquals("a", config.getGroupDatasetApiList().getFirst().getApiId());
            }
            try (var result = statement.executeQuery("SELECT config FROM group_dataset WHERE group_name = 'new'")) {
                assertTrue(result.next());
                var config = json.readValue(result.getString(1), GroupDatasetConfig.class);
                assertEquals("detail", config.getGroupDatasetSpiderList().getFirst().getSpiderId());
            }
        }
    }
}
