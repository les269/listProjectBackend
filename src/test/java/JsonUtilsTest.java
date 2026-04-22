import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.lsb.listProjectBackend.utils.JsonUtils;
import com.lsb.listProjectBackend.utils.Utils;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonUtilsTest {

    @Test
    public void safePut_shouldCreateNestedObjectPath() {
        DocumentContext ctx = JsonPath.parse("{}");

        JsonUtils.safePut(ctx, "$.config.request.url", "https://example.com");

        assertEquals("https://example.com", ctx.read("$.config.request.url", String.class));
    }

    @Test
    public void safePut_shouldWriteArrayObjectPath() {
        DocumentContext ctx = JsonPath.parse("{}");

        JsonUtils.safePut(ctx, "$.items[0].name", "alpha");

        assertEquals("alpha", ctx.read("$.items[0].name", String.class));
    }

    @Test
    public void safePut_shouldSupportNestedArrays() {
        DocumentContext ctx = JsonPath.parse("{}");

        JsonUtils.safePut(ctx, "$.matrix[1][2]", 99);

        assertEquals(99, ctx.read("$.matrix[1][2]", Integer.class));
    }

    @Test
    public void safePut_shouldPreserveExistingObjectAndArrayContent() {
        DocumentContext ctx = JsonPath.parse("{\"existing\":{\"name\":\"keep\"},\"items\":[{\"id\":1}]}");

        JsonUtils.safePut(ctx, "$.existing.version", "v1");
        JsonUtils.safePut(ctx, "$.items[1].id", 2);

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
        String result = Utils.replaceValueByJsonPath(template, root);

        assertEquals("name=Tom, age=18", result);
    }

    @Test
    public void replaceValueByJsonPath_shouldKeepOriginalWhenExpressionIsNotJsonPath() {
        Map<String, Object> root = new HashMap<>();
        root.put("name", "Tom");

        String template = "hello {{name}}";
        String result = Utils.replaceValueByJsonPath(template, root);

        assertEquals("hello {{name}}", result);
    }

    @Test
    public void replaceValueByJsonPath_shouldKeepOriginalWhenPathNotFound() {
        Map<String, Object> user = new HashMap<>();
        user.put("name", "Tom");

        Map<String, Object> root = new HashMap<>();
        root.put("user", user);

        String template = "hello {{$.user.missing}}";
        String result = Utils.replaceValueByJsonPath(template, root);

        assertEquals("hello {{$.user.missing}}", result);
    }
}
