package com.lsb.listProjectBackend.service.spider;

import com.lsb.listProjectBackend.entity.dynamic.common.ReplaceValueMap;
import com.lsb.listProjectBackend.entity.dynamic.spider.*;
import com.lsb.listProjectBackend.repository.dynamic.common.ReplaceValueMapRepository;
import com.lsb.listProjectBackend.utils.Global;
import com.lsb.listProjectBackend.utils.ValuePipelineUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReplaceValueMapPipelineTest {
    static ValuePipeline replacement(String name) {
        var pipeline = new ValuePipeline();
        pipeline.setType(Global.ValuePipelineType.REPLACE_VALUE_MAP);
        pipeline.setEnabled(true);
        pipeline.setUseReplaceValueMap(name);
        return pipeline;
    }

    static ValuePipeline objects(List<ExtractObjectField> fields) {
        var config = new ExtractObjectsConfig();
        config.setMode(Global.ExtractionRuleMode.JSON_PATH);
        config.setFields(fields);
        var pipeline = new ValuePipeline();
        pipeline.setType(Global.ValuePipelineType.EXTRACT_OBJECTS);
        pipeline.setEnabled(true);
        pipeline.setExtractObjectsConfig(config);
        return pipeline;
    }

    static ExtractObjectField field(String key, String path, List<ValuePipeline> pipelines) {
        var field = new ExtractObjectField();
        field.setKey(key);
        field.setJsonPath(path);
        field.setPipelines(pipelines);
        return field;
    }

    @Test
    void replacesStringsAndArraysWithoutChangingUnmatchedOrNonStringValues() {
        var values = new LinkedHashMap<String, Object>();
        values.put("male", "男");
        values.put("blank", " ");
        values.put("empty", null);
        values.put("count", 5);
        var context = ValuePipelineContext.builder().replaceValueMaps(Map.of("gender", values)).build();
        var input = Arrays.asList("MALE", "missing", "blank", "empty", null, 3, Map.of("male", "male"), "count");
        var expected = Arrays.asList("男", "missing", "blank", "empty", null, 3, Map.of("male", "male"), 5);

        assertEquals(expected, ValuePipelineUtils.applyPipelines(List.of(replacement("gender")), input, context));
        assertEquals("男", ValuePipelineUtils.applyPipelines(List.of(replacement("gender")), "Male", context));
        assertEquals("MALE", input.getFirst());
        assertEquals("男", values.get("male"));
    }

    @Test
    void preservesValueWhenContextOrTableIsMissingAndSkipsDisabledPipeline() {
        var pipeline = replacement("missing");
        assertEquals("value", ValuePipelineUtils.applyPipelines(List.of(pipeline), "value", null));
        var context = ValuePipelineContext.builder().replaceValueMaps(Map.of()).build();
        assertNull(ValuePipelineUtils.applyPipelines(List.of(pipeline), null, context));
        assertEquals("value", ValuePipelineUtils.applyPipelines(List.of(replacement(null)), "value", context));
        pipeline.setEnabled(false);
        context.setReplaceValueMaps(Map.of("missing", Map.of("value", "changed")));
        assertEquals("value", ValuePipelineUtils.applyPipelines(List.of(pipeline), "value", context));
    }

    @Test
    void sharesMapsAcrossMultipleLevelsOfObjectExtraction() {
        var pipeline = objects(List.of(field("children", "$.children", List.of(
                objects(List.of(field("gender", "$.detail.gender", List.of(replacement("gender")))))))));
        var context = ValuePipelineContext.builder()
                .replaceValueMaps(Map.of("gender", Map.of("male", "男"))).build();
        var source = Map.of("children", List.of(Map.of("detail", Map.of("gender", "MALE"))));
        assertEquals(List.of(Map.of("children", List.of(Map.of("gender", "男")))),
                ValuePipelineUtils.applyPipelines(List.of(pipeline), source, context));
    }

    @Test
    void batchesUniqueNamesFromEnabledNestedPipelinesAndReturnsReadOnlySnapshot() {
        var repository = mock(ReplaceValueMapRepository.class);
        var table = new ReplaceValueMap();
        table.setName("gender");
        table.setMap(new LinkedHashMap<>(Map.of("male", "男")));
        when(repository.findAllById(any())).thenReturn(List.of(table));
        var disabled = objects(List.of(field("ignored", "$", List.of(replacement("ignored")))));
        disabled.setEnabled(false);
        var nested = objects(List.of(field("gender", "$", List.of(replacement("gender"), replacement("missing")))));

        var maps = new ReplaceValueMapLoader(repository).load(List.of(replacement("gender"), nested, disabled));

        verify(repository).findAllById(Set.of("gender", "missing"));
        verifyNoMoreInteractions(repository);
        assertEquals(Map.of("gender", Map.of("male", "男")), maps);
        table.getMap().put("male", "changed");
        assertEquals("男", maps.get("gender").get("male"));
        assertThrows(UnsupportedOperationException.class, () -> maps.put("new", Map.of()));
        assertThrows(UnsupportedOperationException.class, () -> maps.get("gender").put("male", "changed"));
    }

    @Test
    void doesNotQueryWhenNoEnabledPipelineUsesReplacementTables() {
        var repository = mock(ReplaceValueMapRepository.class);
        var disabled = replacement("gender");
        disabled.setEnabled(false);
        var loader = new ReplaceValueMapLoader(repository);
        assertTrue(loader.load(null).isEmpty());
        assertTrue(loader.load(List.of(disabled, replacement(" "))).isEmpty());
        verifyNoInteractions(repository);
    }
}
