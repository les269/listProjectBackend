import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.lsb.listProjectBackend.utils.JsonUtils;
import com.lsb.listProjectBackend.utils.Utils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonUtilsTest {

    private static final Logger log = LoggerFactory.getLogger(JsonUtilsTest.class);

    @Test
    public void safePut_shouldCreateNestedObjectPath() {
        DocumentContext ctx = JsonPath.parse("{}");

        JsonUtils.putValue(ctx, "$.config.request.url", "https://example.com");

        assertEquals("https://example.com", ctx.read("$.config.request.url", String.class));
    }

    @Test
    public void safePut_shouldWriteArrayObjectPath() {
        DocumentContext ctx = JsonPath.parse("{}");

        JsonUtils.putValue(ctx, "$.items[0].name", "alpha");

        assertEquals("alpha", ctx.read("$.items[0].name", String.class));
    }

    @Test
    public void safePut_shouldSupportNestedArrays() {
        DocumentContext ctx = JsonPath.parse("{}");

        JsonUtils.putValue(ctx, "$.matrix[1][2]", 99);

        assertEquals(99, ctx.read("$.matrix[1][2]", Integer.class));
    }

    @Test
    public void safePut_shouldPreserveExistingObjectAndArrayContent() {
        DocumentContext ctx = JsonPath.parse("{\"existing\":{\"name\":\"keep\"},\"items\":[{\"id\":1}]}");

        JsonUtils.putValue(ctx, "$.existing.version", "v1");
        JsonUtils.putValue(ctx, "$.items[1].id", 2);

        assertEquals("keep", ctx.read("$.existing.name", String.class));
        assertEquals("v1", ctx.read("$.existing.version", String.class));
        assertEquals(1, ctx.read("$.items[0].id", Integer.class));
        assertEquals(2, ctx.read("$.items[1].id", Integer.class));

        List<?> items = ctx.read("$.items", List.class);
        assertEquals(2, items.size());
    }

    @Test
    public void replaceValueByJsonPath_shouldReplaceJsonPathPlaceholders() {
        Map<String, Object> user = new HashMap<>();
        user.put("name", "Tom");
        user.put("age", 18);

        Map<String, Object> root = new HashMap<>();
        root.put("user", user);

        String template = "name={{$.user.name}}, age={{$.user.age}}";
        String result = JsonUtils.replaceValueByJsonPath(template, root);

        assertEquals("name=Tom, age=18", result);
    }

    @Test
    public void replaceValueByJsonPath_shouldKeepOriginalWhenExpressionIsNotJsonPath() {
        Map<String, Object> root = new HashMap<>();
        root.put("name", "Tom");

        String template = "hello {{name}}";
        String result = JsonUtils.replaceValueByJsonPath(template, root);

        assertEquals("hello {{name}}", result);
    }

    @Test
    public void replaceValueByJsonPath_shouldKeepOriginalWhenPathNotFound() {
        Map<String, Object> user = new HashMap<>();
        user.put("name", "Tom");

        Map<String, Object> root = new HashMap<>();
        root.put("user", user);

        String template = "hello {{$.user.missing}}";
        String result = JsonUtils.replaceValueByJsonPath(template, root);

        assertEquals("hello {{$.user.missing}}", result);
    }

    @Test
    public void replaceValueByJsonPath_shouldReplaceWhenRootIsList() {
        List<Map<String, Object>> list = List.of(
                Map.of("name", "Alice"),
                Map.of("name", "Bob"));

        String template = "first={{$[0].name}}, second={{$[1].name}}";
        String result = JsonUtils.replaceValueByJsonPath(template, list);

        assertEquals("first=Alice, second=Bob", result);
    }

    @Test
    public void replaceValueByJsonPath_shouldReplaceNestedFieldInList() {
        List<Map<String, Object>> list = List.of(
                Map.of("user", Map.of("name", "Charlie", "age", 30)));

        String template = "name={{$[0].user.name}}, age={{$[0].user.age}}";
        String result = JsonUtils.replaceValueByJsonPath(template, list);

        assertEquals("name=Charlie, age=30", result);
    }

    @Test
    public void replaceValueByJsonPath_shouldKeepOriginalWhenListIndexOutOfBound() {
        List<Map<String, Object>> list = List.of(
                Map.of("name", "Alice"));

        String template = "hello {{$[5].name}}";
        String result = JsonUtils.replaceValueByJsonPath(template, list);

        assertEquals("hello {{$[5].name}}", result);
    }

    @Test
    public void replaceValueByJsonPath_shouldReturnNullWhenValueIsNull() {
        String result = JsonUtils.replaceValueByJsonPath(null, Map.of("name", "Tom"));
        assertEquals(null, result);
    }

    @Test
    public void replaceValueByJsonPath_shouldReturnEmptyWhenValueIsEmpty() {
        String result = JsonUtils.replaceValueByJsonPath("", Map.of("name", "Tom"));
        assertEquals("", result);
    }

    @Test
    public void replaceValueByJsonPath_shouldReturnOriginalWhenObjIsNull() {
        String result = JsonUtils.replaceValueByJsonPath("hello {{$.name}}", null);
        assertEquals("hello {{$.name}}", result);
    }

    @Test
    public void replaceValueByJsonPath_shouldReturnOriginalWhenNoPlaceholder() {
        String result = JsonUtils.replaceValueByJsonPath("no placeholder here", Map.of("name", "Tom"));
        assertEquals("no placeholder here", result);
    }

    @Test
    public void replaceValueByJsonPath_shouldKeepPlaceholderWhenValueIsNull() {
        Map<String, Object> root = new HashMap<>();
        root.put("key", null);

        String result = JsonUtils.replaceValueByJsonPath("val={{$.key}}", root);
        assertEquals("val={{$.key}}", result);
    }

    @Test
    public void replaceValueByJsonPath_shouldHandleMultiplePlaceholders() {
        var arr = JsonPath.parse("[]");
        arr.add("$", Map.of("name", "Dave"));
        arr.add("$", "test");
        arr.add("$", List.of(1, 2, 3));
        arr.add("$", 123);
        arr.add("$", true);
        arr.json();
        log.info("arr={}", arr.jsonString());
    }
}
