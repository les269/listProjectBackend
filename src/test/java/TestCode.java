
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.lsb.listProjectBackend.utils.JsonUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCode {
    private static final Logger log = LoggerFactory.getLogger(TestCode.class);

    /**
     * 測試 replaceValueByJsonPath 傳入 DocumentContext.json()（String）能否正確替換
     */
    @Test
    public void testReplaceValueByJsonPath_withDocumentContextJson() {
        // 準備 DocumentContext，模擬爬蟲執行過程中累積的 result
        DocumentContext ctx = JsonPath.parse("{}");
        ctx.put("$", "title", "測試標題");
        ctx.put("$", "price", 99);
        ctx.put("$", "tags", java.util.List.of("java", "spring"));

        String json = ctx.jsonString();
        log.info("DocumentContext.json() = {}", json);

        // 測試 dot-notation
        String template1 = "商品：{{$['title']}}，價格：{{$.price}}";
        String result1 = JsonUtils.replaceValueByJsonPath(template1, ctx.json());
        log.info("json: {}", ctx.json().toString());
        log.info("dot-notation result: {}", result1);
    }
}
