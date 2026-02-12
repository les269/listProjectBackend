import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lsb.listProjectBackend.utils.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import javax.script.*;
import java.io.*;
import java.util.List;
import java.util.Map;

import static com.lsb.listProjectBackend.utils.Utils.replaceValue;

public class TestCode {


    @Test
    public void test3() throws IOException {
        File file =new File("Z:/");
        System.out.println(file.getFreeSpace());
        System.out.println(file.getUsableSpace());
    }
}
