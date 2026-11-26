package com.lsb.listProjectBackend;

import com.jayway.jsonpath.JsonPath;
import com.lsb.listProjectBackend.service.spider.ReplaceValueMapLoader;
import com.lsb.listProjectBackend.domain.spider.SpiderConfigTO;
import com.lsb.listProjectBackend.domain.spider.SpiderItemTO;
import com.lsb.listProjectBackend.entity.dynamic.spider.ExtractionRule;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderItemSetting;
import com.lsb.listProjectBackend.entity.dynamic.spider.ValuePipeline;
import com.lsb.listProjectBackend.service.common.CookieListService;
import com.lsb.listProjectBackend.service.common.HeadersService;
import com.lsb.listProjectBackend.service.spider.ExtractionRuleServiceImpl;
import com.lsb.listProjectBackend.service.spider.SpiderConfigService;
import com.lsb.listProjectBackend.service.spider.SpiderExecutionService;
import com.lsb.listProjectBackend.service.spider.SpiderItemService;
import com.lsb.listProjectBackend.service.spider.SpiderServiceImpl;
import com.lsb.listProjectBackend.utils.Global;
import org.jsoup.Connection;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SpiderEmptyUrlTest {
    private final CookieListService cookies = mock(CookieListService.class);
    private final HeadersService headers = mock(HeadersService.class);
    private final SpiderExecutionService execution = spy(new SpiderExecutionService(
            cookies, new ExtractionRuleServiceImpl(), headers,
                    new ReplaceValueMapLoader(mock(com.lsb.listProjectBackend.repository.dynamic.common.ReplaceValueMapRepository.class))));

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void emptyUrlUsesEmptyHtmlAndRunsPipelines(String url) throws Exception {
        var result = JsonPath.parse("{\"existing\":true}");
        var body = new ExtractionRule();
        body.setKey("body");
        body.setSelector("body");

        execution.executeRequest(url, Global.ExtractionRuleMode.SELECT, List.of(body, fixedRule()),
                null, null, result, 15L);

        assertEquals(List.of(), result.read("$.body"));
        assertEquals("seed", result.read("$.seed"));
        assertEquals(Boolean.TRUE, result.read("$.existing"));
        verify(execution, never()).getConnection(any());
        verify(execution, never()).waitForRequestInterval(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void emptyUrlUsesEmptyJsonAndMissingPathsStillRunPipelines(String url) throws Exception {
        var root = new ExtractionRule();
        root.setKey("root");
        root.setJsonPath("$");
        var result = JsonPath.parse("{}");

        execution.executeRequest(url, Global.ExtractionRuleMode.JSON_PATH, List.of(root, fixedRule()),
                null, null, result, 15L);

        assertEquals(Map.of(), result.read("$.root"));
        assertEquals("seed", result.read("$.seed"));
        verify(execution, never()).getConnection(any());
        verify(execution, never()).waitForRequestInterval(any());
    }

    @ParameterizedTest
    @EnumSource(Global.ExtractionRuleMode.class)
    void primeKeyFlowUsesEmptyStepResultInNextUrlWithoutInitialDelay(Global.ExtractionRuleMode mode)
            throws Exception {
        var service = serviceWithTwoSteps(mode);

        assertEquals("seed", JsonPath.read(service.executeByPrimeKeyList("spider", List.of()), "$.seed"));

        verify(execution).getConnection("https://example.test/seed");
        verify(execution).waitForRequestInterval(null);
    }

    @ParameterizedTest
    @EnumSource(Global.ExtractionRuleMode.class)
    void urlFlowUsesEmptyStepResultInNextUrlWithoutInitialDelay(Global.ExtractionRuleMode mode)
            throws Exception {
        var service = serviceWithTwoSteps(mode);

        assertEquals("seed", JsonPath.read(service.executeByUrl("spider", ""), "$.seed"));

        verify(execution).getConnection("https://example.test/seed");
        verify(execution).waitForRequestInterval(null);
    }

    private SpiderServiceImpl serviceWithTwoSteps(Global.ExtractionRuleMode mode) throws Exception {
        var configs = mock(SpiderConfigService.class);
        var items = mock(SpiderItemService.class);
        when(configs.getById("spider"))
                .thenReturn(new SpiderConfigTO("spider", "", 1, null, false, null, 15L));
        var initialize = new SpiderItemSetting();
        initialize.setUrl("");
        initialize.setUrlType(Global.SpiderUrlType.BY_PARAMS);
        initialize.setMode(mode);
        initialize.setExtractionRuleList(List.of(fixedRule()));
        var fetch = new SpiderItemSetting();
        fetch.setUrl("https://example.test/{{$.seed}}");
        fetch.setUrlType(Global.SpiderUrlType.BY_PARAMS);
        fetch.setMode(mode);
        fetch.setExtractionRuleList(List.of());
        when(items.getBySpiderId("spider")).thenReturn(List.of(
                new SpiderItemTO("initialize", "", initialize, null),
                new SpiderItemTO("fetch", "", fetch, null)));
        when(cookies.getMapByRefIdsAndType(anyList(), any())).thenReturn(Map.of());
        when(headers.getMapByRefIdsAndType(anyList(), any())).thenReturn(Map.of());
        doReturn(mock(Connection.class)).when(execution).getConnection("https://example.test/seed");
        doNothing().when(execution).useExtraction(any(), any(), anyList(), any(), anyList());
        return new SpiderServiceImpl(configs, items, cookies, execution, headers, null);
    }

    private ExtractionRule fixedRule() {
        var pipeline = new ValuePipeline();
        pipeline.setEnabled(true);
        pipeline.setType(Global.ValuePipelineType.FIXED_VALUE);
        pipeline.setFixedValue("seed");
        var rule = new ExtractionRule();
        rule.setKey("seed");
        rule.setSelector(".missing");
        rule.setJsonPath("$.missing");
        rule.setPipelines(List.of(pipeline));
        return rule;
    }
}
