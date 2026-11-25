package com.lsb.listProjectBackend;

import com.jayway.jsonpath.JsonPath;
import com.lsb.listProjectBackend.domain.spider.SpiderConfigTO;
import com.lsb.listProjectBackend.domain.spider.SpiderItemTO;
import com.lsb.listProjectBackend.domain.spider.SpiderReqTO;
import com.lsb.listProjectBackend.entity.dynamic.spider.ExtractionRule;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderItemSetting;
import com.lsb.listProjectBackend.entity.dynamic.spider.ValuePipeline;
import com.lsb.listProjectBackend.service.common.CookieListService;
import com.lsb.listProjectBackend.service.common.HeadersService;
import com.lsb.listProjectBackend.service.spider.*;
import com.lsb.listProjectBackend.utils.Global;
import org.jsoup.Connection;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SpiderFailureContinuationTest {
    private final SpiderConfigService configs = mock(SpiderConfigService.class);
    private final SpiderItemService items = mock(SpiderItemService.class);
    private final CookieListService cookies = mock(CookieListService.class);
    private final HeadersService headers = mock(HeadersService.class);
    private final SpiderExecutionService execution = spy(new SpiderExecutionService(
            cookies, new ExtractionRuleServiceImpl(), headers));
    private final SpiderServiceImpl service = new SpiderServiceImpl(configs, items, cookies, execution, headers, null);

    @ParameterizedTest
    @EnumSource(Global.ExtractionRuleMode.class)
    void unresolvedUrlDoesNotDiscardPrimaryKeysOrStopLaterStepsAndRecords(Global.ExtractionRuleMode mode)
            throws Exception {
        setup(mode, "{{$.workno}}?locale=zh_TW");

        var results = service.executeBatch("detail", List.of(
                new SpiderReqTO("detail", null, List.of("one")),
                new SpiderReqTO("detail", null, List.of("two")),
                new SpiderReqTO("detail", null, List.of("three"))));

        assertEquals(List.of("one", "two", "three"),
                results.stream().map(json -> JsonPath.<String>read(json, "$.id")).toList());
        assertEquals(List.of("done", "done", "done"),
                results.stream().map(json -> JsonPath.<String>read(json, "$.after")).toList());
        verify(execution, times(3)).getConnection("{{$.workno}}?locale=zh_TW");
    }

    @ParameterizedTest
    @EnumSource(Global.ExtractionRuleMode.class)
    void timeoutDoesNotStopBatchOrDiscardCollectedData(Global.ExtractionRuleMode mode) throws Exception {
        setup(mode, "https://example.test/timeout");
        var connection = mock(Connection.class);
        doReturn(connection).when(execution).getConnection("https://example.test/timeout");
        when(connection.ignoreContentType(true)).thenReturn(connection);
        when(connection.get()).thenThrow(new SocketTimeoutException("test timeout"));

        var results = service.executeBatch("detail", List.of(
                new SpiderReqTO("detail", null, List.of("one")),
                new SpiderReqTO("detail", null, List.of("two"))));

        assertEquals(2, results.size());
        assertEquals("one", JsonPath.read(results.getFirst(), "$.id"));
        assertEquals("two", JsonPath.read(results.getLast(), "$.id"));
        assertEquals("done", JsonPath.read(results.getLast(), "$.after"));
        verify(connection, times(2)).get();
    }

    @ParameterizedTest
    @EnumSource(Global.ExtractionRuleMode.class)
    void urlEntryContinuesAfterMalformedFirstUrl(Global.ExtractionRuleMode mode) throws Exception {
        setup(mode, "{{$.workno}}?locale=zh_TW");

        var result = service.executeByUrl("detail", "{{$.workno}}?locale=zh_TW");

        assertEquals("done", JsonPath.read(result, "$.after"));
    }

    private void setup(Global.ExtractionRuleMode mode, String failingUrl) {
        when(configs.getById("detail"))
                .thenReturn(new SpiderConfigTO("detail", "", 1, null, false, null, 0L));
        when(cookies.getMapByRefIdsAndType(anyList(), any())).thenReturn(Map.of());
        when(headers.getMapByRefIdsAndType(anyList(), any())).thenReturn(Map.of());
        var keys = pipeline(Global.ValuePipelineType.INITIAL_KEYS);
        var first = pipeline(Global.ValuePipelineType.FIRST_VALUE);
        first.setSeq(1);
        var fixed = pipeline(Global.ValuePipelineType.FIXED_VALUE);
        fixed.setFixedValue("done");
        when(items.getBySpiderId("detail")).thenReturn(List.of(
                item("initialize", "", mode, rule("id", List.of(keys, first))),
                item("failure", failingUrl, mode, rule("unused", List.of())),
                item("after", "", mode, rule("after", List.of(fixed)))));
    }

    private ValuePipeline pipeline(Global.ValuePipelineType type) {
        var pipeline = new ValuePipeline();
        pipeline.setEnabled(true);
        pipeline.setType(type);
        return pipeline;
    }

    private ExtractionRule rule(String key, List<ValuePipeline> pipelines) {
        var rule = new ExtractionRule();
        rule.setKey(key);
        rule.setJsonPath("$.missing");
        rule.setPipelines(pipelines);
        return rule;
    }

    private SpiderItemTO item(String id, String url, Global.ExtractionRuleMode mode, ExtractionRule rule) {
        var setting = new SpiderItemSetting();
        setting.setUrl(url);
        setting.setUrlType(Global.SpiderUrlType.BY_PARAMS);
        setting.setMode(mode);
        setting.setExtractionRuleList(List.of(rule));
        return new SpiderItemTO(id, "", setting, null);
    }
}
