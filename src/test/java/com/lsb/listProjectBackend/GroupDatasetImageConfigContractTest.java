package com.lsb.listProjectBackend;

import com.lsb.listProjectBackend.entity.dynamic.dataset.DatasetConfig;
import com.lsb.listProjectBackend.entity.dynamic.dataset.GroupDatasetConfig;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

class GroupDatasetImageConfigContractTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void groupSettingsRoundTripWithImageDownloadOptions() {
        var config = mapper.readValue("""
                {"byKey":"id","autoImageDownload":true,"imageByKey":"cover","imageSaveFolder":"images"}
                """, GroupDatasetConfig.class);
        assertTrue(config.isAutoImageDownload());
        assertEquals("cover", config.getImageByKey());
        var json = mapper.valueToTree(config);
        assertTrue(json.get("autoImageDownload").asBoolean());
        assertEquals("cover", json.get("imageByKey").asString());
        assertEquals("images", json.get("imageSaveFolder").asString());
    }

    @Test
    void omittedGroupImageSettingsDefaultToDisabled() {
        var config = mapper.readValue("{\"byKey\":\"id\"}", GroupDatasetConfig.class);
        assertFalse(config.isAutoImageDownload());
        assertNull(config.getImageByKey());
    }

    @Test
    void legacyDatasetImageSettingsAreIgnoredAndOmittedFromOutput() {
        var config = mapper.readValue("""
                {"groupName":"group","autoImageDownload":true,"imageByKey":"legacy"}
                """, DatasetConfig.class);
        assertEquals("group", config.getGroupName());
        var json = mapper.valueToTree(config);
        assertFalse(json.has("autoImageDownload"));
        assertFalse(json.has("imageByKey"));
    }
}
