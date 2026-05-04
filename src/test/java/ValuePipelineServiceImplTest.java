import com.jayway.jsonpath.JsonPath;
import com.lsb.listProjectBackend.domain.common.ReplaceValueMapTO;
import com.lsb.listProjectBackend.entity.dynamic.spider.ValuePipeline;
import com.lsb.listProjectBackend.entity.dynamic.spider.ValuePipelineContext;
import com.lsb.listProjectBackend.service.common.ReplaceValueMapService;
import com.lsb.listProjectBackend.service.spider.ValuePipelineServiceImpl;
import com.lsb.listProjectBackend.utils.Global;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ValuePipelineServiceImplTest {

        @Test
        public void fetchReplaceValueMaps_shouldLoadDistinctEnabledReplaceMapsOnly() {
                ReplaceValueMapService replaceValueMapService = mock(ReplaceValueMapService.class);
                ValuePipelineServiceImpl service = new ValuePipelineServiceImpl(replaceValueMapService);

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

                ReplaceValueMapTO replaceValueMapTO = new ReplaceValueMapTO("status-map",
                                Map.of("A", "Alpha", "B", "Beta"),
                                null, null);
                when(replaceValueMapService.getAllByNameList(eq(List.of("status-map"))))
                                .thenReturn(List.of(replaceValueMapTO));

                List<ReplaceValueMapTO> result = service
                                .fetchReplaceValueMaps(
                                                List.of(enabledReplaceMap, duplicateReplaceMap, disabledReplaceMap));

                assertEquals(1, result.size());
                assertEquals("status-map", result.getFirst().name());
                verify(replaceValueMapService).getAllByNameList(eq(List.of("status-map")));
        }

        @Test
        public void applyPipelines_shouldProcessElementsInSeqOrder() {
                ValuePipelineServiceImpl service = new ValuePipelineServiceImpl(mock(ReplaceValueMapService.class));

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
                var context = new ValuePipelineContext(
                                List.of(),
                                JsonPath.parse("{}"),
                                Jsoup.parse("<a href='https://example.com/item-1'>One</a><a href='https://example.com/item-2'>Two</a>")
                                                .select("a"));

                Object result = service.applyPipelines(pipelines, null, context);

                assertEquals("item-1", result);
        }

        @Test
        public void applyPipelines_shouldUseReplaceValueMapForListValues() {
                ValuePipelineServiceImpl service = new ValuePipelineServiceImpl(mock(ReplaceValueMapService.class));

                ValuePipeline pipeline = new ValuePipeline();
                pipeline.setSeq(1);
                pipeline.setEnabled(true);
                pipeline.setType(Global.ValuePipelineType.USE_REPLACE_VALUE_MAP);
                pipeline.setUseReplaceValueMap("status-map");

                ReplaceValueMapTO replaceValueMapTO = new ReplaceValueMapTO("status-map",
                                Map.of("A", "Alpha", "B", "Beta"),
                                null, null);
                when(service.fetchReplaceValueMaps(List.of(pipeline))).thenReturn(List.of(replaceValueMapTO));

                var context = new ValuePipelineContext(List.of(replaceValueMapTO), JsonPath.parse("{}"),
                                null);

                Object result = service.applyPipelines(List.of(pipeline), List.of("A", "B", "C"), context);

                assertIterableEquals(List.of("Alpha", "Beta", "C"), (List<?>) result);
        }

        @Test
        public void normalizeExtractedValue_shouldTrimFilterAndDeduplicateStringList() {
                ValuePipelineServiceImpl service = new ValuePipelineServiceImpl(mock(ReplaceValueMapService.class));

                Object result = service.normalizeExtractedValue(Arrays.asList(" A ", "A", "", "   ", null, "B "));

                assertIterableEquals(List.of("A", "B"), (List<?>) result);
        }
}
