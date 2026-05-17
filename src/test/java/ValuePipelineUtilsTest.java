import com.jayway.jsonpath.JsonPath;
import com.lsb.listProjectBackend.entity.dynamic.spider.ValuePipeline;
import com.lsb.listProjectBackend.entity.dynamic.spider.ValuePipelineContext;
import com.lsb.listProjectBackend.utils.Global;
import com.lsb.listProjectBackend.utils.ValuePipelineUtils;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class ValuePipelineUtilsTest {

    @Test
    public void applyPipelines_shouldProcessElementsInSeqOrder() {
        ValuePipeline extractAttr = new ValuePipeline();
        extractAttr.setSeq(1);
        extractAttr.setEnabled(true);
        extractAttr.setType(Global.ValuePipelineType.EXTRACT_ATTR);
        extractAttr.setAttributeName("href");

        ValuePipeline firstValue = new ValuePipeline();
        firstValue.setSeq(3);
        firstValue.setEnabled(true);
        firstValue.setType(Global.ValuePipelineType.FIRST_VALUE);

        ValuePipeline replaceRegular = new ValuePipeline();
        replaceRegular.setSeq(2);
        replaceRegular.setEnabled(true);
        replaceRegular.setType(Global.ValuePipelineType.REPLACE_REGULAR);
        replaceRegular.setPattern("https://example.com/");
        replaceRegular.setReplacement("");

        List<ValuePipeline> pipelines = List.of(extractAttr, firstValue, replaceRegular);
        var context = ValuePipelineContext.builder()
                .result(JsonPath.parse("{}"))
                .elements(Jsoup.parse(
                        "<a href='https://example.com/item-1'>One</a><a href='https://example.com/item-2'>Two</a>")
                        .select("a"))
                .build();

        Object result = ValuePipelineUtils.applyPipelines(pipelines, null, context);

        assertEquals("item-1", result);
    }

    @Test
    public void normalizeExtractedValue_shouldTrimFilterAndDeduplicateStringList() {
        Object result = ValuePipelineUtils.normalizeExtractedValue(Arrays.asList(" A ", "A", "", "   ", null, "B "));

        assertIterableEquals(List.of("A", "B"), (List<?>) result);
    }
}
