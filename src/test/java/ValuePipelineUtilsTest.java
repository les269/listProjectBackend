import com.jayway.jsonpath.JsonPath;
import com.lsb.listProjectBackend.entity.dynamic.spider.CalculateConfig;
import com.lsb.listProjectBackend.entity.dynamic.spider.ValuePipeline;
import com.lsb.listProjectBackend.entity.dynamic.spider.ValuePipelineContext;
import com.lsb.listProjectBackend.utils.Global;
import com.lsb.listProjectBackend.utils.ValuePipelineUtils;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValuePipelineUtilsTest {

    @Test
    public void initialKeys_shouldReturnOriginalOrderAndAllowTemplateCombination() {
        var keys = new ValuePipeline();
        keys.setEnabled(true);
        keys.setType(Global.ValuePipelineType.INITIAL_KEYS);
        var context = ValuePipelineContext.builder().initialKeys(List.of(" A ", "A", "B")).build();
        assertEquals(List.of(" A ", "A", "B"), ValuePipelineUtils.applyPipeline(keys, "previous", context));

        var combine = new ValuePipeline();
        combine.setEnabled(true);
        combine.setSeq(1);
        combine.setType(Global.ValuePipelineType.COMBINE_TO_STRING);
        combine.setCombineToString("{{$[1]}}-{{$[2]}}");
        assertEquals("A-B", ValuePipelineUtils.applyPipelines(List.of(keys, combine), null, context));
    }

    @Test
    public void initialKeys_shouldReturnEmptyListWhenNoKeysWereProvided() {
        var keys = new ValuePipeline();
        keys.setType(Global.ValuePipelineType.INITIAL_KEYS);
        assertEquals(List.of(), ValuePipelineUtils.applyPipeline(keys, "previous", null));
        assertEquals(List.of(), ValuePipelineUtils.applyPipeline(keys, "previous",
                ValuePipelineContext.builder().build()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"#nowMillis()", "#nowSeconds()"})
    public void calculate_shouldGenerateTimestampWithoutVariablePaths(String expression) {
        var config = new CalculateConfig();
        config.setExpression(expression);
        var pipeline = new ValuePipeline();
        pipeline.setEnabled(true);
        pipeline.setType(Global.ValuePipelineType.CALCULATE);
        pipeline.setCalculateConfig(config);
        long divisor = expression.equals("#nowMillis()") ? 1 : 1000;
        long before = System.currentTimeMillis() / divisor;

        Object result = ValuePipelineUtils.applyPipelines(List.of(pipeline), null, null);

        long after = System.currentTimeMillis() / divisor;
        assertInstanceOf(String.class, result);
        long timestamp = Long.parseLong((String) result);
        assertTrue(timestamp >= before && timestamp <= after);
    }

    @Test
    public void applyPipelines_shouldCombineElementTextAsFirstStep() {
        ValuePipeline combine = new ValuePipeline();
        combine.setEnabled(true);
        combine.setType(Global.ValuePipelineType.COMBINE_TO_STRING);
        combine.setCombineToString("{{$[0]}} / {{$[1]}}");
        var context = ValuePipelineContext.builder()
                .elements(Jsoup.parse("<span>One</span><span>Two</span>").select("span"))
                .build();

        Object result = ValuePipelineUtils.applyPipelines(List.of(combine), null, context);

        assertEquals("One / Two", result);
    }

    @Test
    public void applyPipelines_shouldCombineExistingValueInsteadOfElementText() {
        ValuePipeline combine = new ValuePipeline();
        combine.setEnabled(true);
        combine.setType(Global.ValuePipelineType.COMBINE_TO_STRING);
        combine.setCombineToString("{{$[0]}} / {{$[1]}}");
        var context = ValuePipelineContext.builder()
                .elements(Jsoup.parse("<span>One</span><span>Two</span>").select("span"))
                .build();

        Object result = ValuePipelineUtils.applyPipelines(List.of(combine), List.of("A", "B"), context);

        assertEquals("A / B", result);
    }

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
