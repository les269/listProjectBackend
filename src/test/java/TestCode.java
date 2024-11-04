import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lsb.listProjectBackend.utils.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import javax.script.*;
import java.io.*;
import java.util.List;
import java.util.Map;

import static com.lsb.listProjectBackend.utils.Utils.replaceValue;

public class TestCode {

    @Test
    public void testJS2() throws ScriptException, FileNotFoundException {
        String filePath = "C:\\code\\testfile\\test.html";
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Document doc = Jsoup.parse(contentBuilder.toString());
    }

    @Test
    public void testReplace() {
        String template = "http://localhost/search?name=${user.name}&age=${user.age}&tags=${user.tags[0].key}";
        Map<String, Object> user = Map.of(
                "name", "John",
                "age", 25,
                "tags", List.of(Map.of("key", "test"))
        );
        Map<String, Object> data = Map.of("user", user);

        String result = replaceValue(template, data);
        System.out.println(result);
    }

    @Test
    public void test2() throws JsonProcessingException {
        var  objectMapper = new ObjectMapper();
        var object=objectMapper.readValue("{\"a\":{\"b\":{\"c\":\"123\"}}}",Object.class);
        System.out.println(object);
    }
    @Test
    public void test3() throws IOException {
    }
}
