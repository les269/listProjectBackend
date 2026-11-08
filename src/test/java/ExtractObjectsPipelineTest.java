import com.jayway.jsonpath.JsonPath;
import com.lsb.listProjectBackend.entity.dynamic.spider.*;
import com.lsb.listProjectBackend.utils.Global;
import com.lsb.listProjectBackend.utils.ValuePipelineUtils;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExtractObjectsPipelineTest {
    @Test
    void extractsRowsWithChildPipelinesAndKeepsMissingAndIdenticalRows() {
        var name = field("name", "a", null);
        var url = field("url", "a", null);
        var attr = pipeline(Global.ValuePipelineType.EXTRACT_ATTR);
        attr.setAttributeName("href");
        url.setPipelines(List.of(attr, pipeline(Global.ValuePipelineType.FIRST_VALUE)));
        var elements = Jsoup.parse("<table><tr><td><a href='/a'>A</a></td></tr>"
                + "<tr><td>missing</td></tr><tr><td><a href='/a'>A</a></td></tr></table>").select("tr");
        var context = ValuePipelineContext.builder().elements(elements).build();
        var result = extract(Global.ExtractionRuleMode.SELECT, List.of(name, url), null, context);
        assertEquals(3, result.size());
        assertEquals(Map.of("name", "A", "url", "/a"), result.getFirst());
        assertTrue(result.get(1).containsKey("name"));
        assertNull(result.get(1).get("name"));
        assertEquals(result.getFirst(), result.getLast());
        assertEquals(3, ((List<?>) ValuePipelineUtils.normalizeExtractedValue(result)).size());
    }

    @Test
    void jsonFieldsUseTheirOwnRowAndCanReadEarlierFieldsAndParentContext() {
        var id = field("id", null, "id");
        id.setSeq(1);
        var copy = field("copy", null, "missing");
        copy.setSeq(2);
        var current = pipeline(Global.ValuePipelineType.CURRENT_DATA_KEY);
        current.setCurrentDataKey("$.id");
        copy.setPipelines(List.of(current));
        var outer = field("outer", null, "missing");
        var fetch = pipeline(Global.ValuePipelineType.FETCH_CURRENT_DATA);
        fetch.setFetchCurrentDataKey("$.group");
        outer.setPipelines(List.of(fetch));
        var empty = field("empty", null, "empty");
        var context = ValuePipelineContext.builder().result(JsonPath.parse("{\"group\":\"g\"}")).build();
        Object input = JsonPath.parse("[{\"id\":\"1\",\"empty\":\"\"},{\"id\":\"2\"}]").json();
        var result = extract(Global.ExtractionRuleMode.JSON_PATH, List.of(copy, id, outer, empty), input, context);
        assertEquals("1", result.getFirst().get("copy"));
        assertEquals("2", result.getLast().get("copy"));
        assertEquals("g", result.getFirst().get("outer"));
        assertEquals("", result.getFirst().get("empty"));
        assertNull(result.getLast().get("empty"));
    }

    @Test
    void supportsNestedObjectExtraction() {
        var tags = field("tags", null, "tags");
        var nested = pipeline(Global.ValuePipelineType.EXTRACT_OBJECTS);
        var nestedConfig = new ExtractObjectsConfig();
        nestedConfig.setMode(Global.ExtractionRuleMode.JSON_PATH);
        nestedConfig.setFields(List.of(field("name", null, "name")));
        nested.setExtractObjectsConfig(nestedConfig);
        tags.setPipelines(List.of(nested));
        var result = extract(Global.ExtractionRuleMode.JSON_PATH, List.of(tags),
                JsonPath.parse("[{\"tags\":[{\"name\":\"x\"},{\"name\":\"x\"}]}]").json(), null);
        assertEquals(List.of(Map.of("name", "x"), Map.of("name", "x")), result.getFirst().get("tags"));
    }

    @Test
    void noMatchesReturnsEmptyListAndInvalidConfigFails() {
        assertEquals(List.of(), extract(Global.ExtractionRuleMode.SELECT, List.of(field("name", "a", null)),
                null, ValuePipelineContext.builder().elements(Jsoup.parse("").select("tr")).build()));
        assertThrows(IllegalArgumentException.class, () -> ValuePipelineUtils.applyPipeline(
                pipeline(Global.ValuePipelineType.EXTRACT_OBJECTS), null, null));
        assertThrows(IllegalArgumentException.class, () -> extract(Global.ExtractionRuleMode.JSON_PATH,
                List.of(field("id", null, "id"), field("id", null, "other")), List.of(), null));
    }

    @Test
    void convertToArrayAcceptsNull() {
        var result = (List<?>) ValuePipelineUtils.applyPipeline(pipeline(Global.ValuePipelineType.CONVERT_TO_ARRAY), null, null);
        assertEquals(1, result.size());
        assertNull(result.getFirst());
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extract(Global.ExtractionRuleMode mode, List<ExtractObjectField> fields,
            Object input, ValuePipelineContext context) {
        var config = new ExtractObjectsConfig();
        config.setMode(mode);
        config.setFields(fields);
        var pipeline = pipeline(Global.ValuePipelineType.EXTRACT_OBJECTS);
        pipeline.setExtractObjectsConfig(config);
        return (List<Map<String, Object>>) ValuePipelineUtils.applyPipeline(pipeline, input, context);
    }

    private ExtractObjectField field(String key, String selector, String path) {
        var field = new ExtractObjectField();
        field.setKey(key);
        field.setSelector(selector);
        field.setJsonPath(path);
        return field;
    }

    private ValuePipeline pipeline(Global.ValuePipelineType type) {
        var pipeline = new ValuePipeline();
        pipeline.setType(type);
        pipeline.setEnabled(true);
        return pipeline;
    }
}
