import com.jayway.jsonpath.JsonPath;
import com.lsb.listProjectBackend.domain.common.ReplaceValueMapTO;
import com.lsb.listProjectBackend.entity.dynamic.spider.ValuePipeline;
import com.lsb.listProjectBackend.entity.dynamic.spider.ValuePipelineContext;
import com.lsb.listProjectBackend.utils.Global;
import com.lsb.listProjectBackend.utils.ValuePipelineUtils;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class ValuePipelineUtilsTest {

    @Test
    public void getReplaceValueNameList_shouldReturnDistinctEnabledReplaceMapsOnly() {
        ValuePipeline enabledReplaceMap = new ValuePipeline();
        enabledReplaceMap.setEnabled(true);
        enabledReplaceMap.setType(Global.ValuePipelineType.USE_REPLACE_VALUE_MAP);
        enabledReplaceMap.setUseReplaceValueMap("status-map");

        ValuePipeline duplicateReplaceMap = new ValuePipeline();
        duplicateReplaceMap.setEnabled(true);
        duplicateReplaceMap.setType(Global.ValuePipelineType.USE_REPLACE_VALUE_MAP);
        duplicateReplaceMap.setUseReplaceValueMap("status-map");

        ValuePipeline disabledReplaceMap = new ValuePipeline();
        disabledReplaceMap.setEnabled(false);
        disabledReplaceMap.setType(Global.ValuePipelineType.USE_REPLACE_VALUE_MAP);
        disabledReplaceMap.setUseReplaceValueMap("ignored-map");

        List<String> result = ValuePipelineUtils.getReplaceValueNameList(
                List.of(enabledReplaceMap, duplicateReplaceMap, disabledReplaceMap));

        assertEquals(List.of("status-map"), result);
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
                .replaceValueMapList(List.of())
                .result(JsonPath.parse("{}"))
                .elements(Jsoup.parse(
                        "<a href='https://example.com/item-1'>One</a><a href='https://example.com/item-2'>Two</a>")
                        .select("a"))
                .build();

        Object result = ValuePipelineUtils.applyPipelines(pipelines, null, context);

        assertEquals("item-1", result);
    }

    @Test
    public void applyPipelines_shouldUseReplaceValueMapForListValues() {
        ValuePipeline pipeline = new ValuePipeline();
        pipeline.setSeq(1);
        pipeline.setEnabled(true);
        pipeline.setType(Global.ValuePipelineType.USE_REPLACE_VALUE_MAP);
        pipeline.setUseReplaceValueMap("status-map");

        ReplaceValueMapTO replaceValueMapTO = new ReplaceValueMapTO("status-map",
                Map.of("A", "Alpha", "B", "Beta"),
                null, null);

        var context = ValuePipelineContext.builder()
                .replaceValueMapList(List.of(replaceValueMapTO))
                .result(JsonPath.parse("{}"))
                .build();

        Object result = ValuePipelineUtils.applyPipelines(List.of(pipeline), List.of("A", "B", "C"), context);

        assertIterableEquals(List.of("Alpha", "Beta", "C"), (List<?>) result);
    }

    @Test
    public void normalizeExtractedValue_shouldTrimFilterAndDeduplicateStringList() {
        Object result = ValuePipelineUtils.normalizeExtractedValue(Arrays.asList(" A ", "A", "", "   ", null, "B "));

        assertIterableEquals(List.of("A", "B"), (List<?>) result);
    }
}
