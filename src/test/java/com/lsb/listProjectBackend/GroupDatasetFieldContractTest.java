package com.lsb.listProjectBackend;

import com.lsb.listProjectBackend.entity.dynamic.dataset.GroupDatasetField;
import com.lsb.listProjectBackend.utils.Global;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

class GroupDatasetFieldContractTest {
    @Test
    void ignoresLegacyReplacementSettingAndOmitsItFromOutput() {
        var mapper = new ObjectMapper();
        var field = mapper.readValue("""
                {"seq":1,"type":"string","key":"name","label":"Name",
                 "replaceValueMapName":"legacy"}
                """, GroupDatasetField.class);

        assertEquals("name", field.getKey());
        assertEquals("Name", field.getLabel());
        assertEquals(Global.GroupDatasetFieldType.string, field.getType());
        assertFalse(mapper.valueToTree(field).has("replaceValueMapName"));
    }
}
