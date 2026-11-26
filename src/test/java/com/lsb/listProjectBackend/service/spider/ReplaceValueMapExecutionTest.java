package com.lsb.listProjectBackend.service.spider;

import com.jayway.jsonpath.JsonPath;
import com.lsb.listProjectBackend.domain.common.CookieListTO;
import com.lsb.listProjectBackend.domain.common.HeadersTO;
import com.lsb.listProjectBackend.domain.spider.SpiderConfigTO;
import com.lsb.listProjectBackend.domain.spider.SpiderItemTO;
import com.lsb.listProjectBackend.domain.spider.SpiderReqTO;
import com.lsb.listProjectBackend.entity.dynamic.common.Cookie;
import com.lsb.listProjectBackend.entity.dynamic.common.Header;
import com.lsb.listProjectBackend.entity.dynamic.common.ReplaceValueMap;
import com.lsb.listProjectBackend.entity.dynamic.spider.*;
import com.lsb.listProjectBackend.repository.dynamic.common.ReplaceValueMapRepository;
import com.lsb.listProjectBackend.service.common.CookieListService;
import com.lsb.listProjectBackend.service.common.HeadersService;
import com.lsb.listProjectBackend.utils.Global;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static com.lsb.listProjectBackend.service.spider.ReplaceValueMapPipelineTest.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReplaceValueMapExecutionTest {
    private final ReplaceValueMapRepository repository = mock(ReplaceValueMapRepository.class);
    private final CookieListService cookies = mock(CookieListService.class);
    private final HeadersService headers = mock(HeadersService.class);
    private final SpiderExecutionService execution = new SpiderExecutionService(cookies,
            new ExtractionRuleServiceImpl(), headers, new ReplaceValueMapLoader(repository));

    private void stubTable(String value) {
        var table = new ReplaceValueMap();
        table.setName("gender");
        table.setMap(Map.of("male", value));
        when(repository.findAllById(any())).thenReturn(List.of(table));
    }

    private ExtractionRule rule(String key, String path, List<ValuePipeline> pipelines) {
        var rule = new ExtractionRule();
        rule.setKey(key);
        rule.setJsonPath(path);
        rule.setPipelines(pipelines);
        return rule;
    }

    @Test
    void hundredRequestsShareOneSnapshotIncludingNestedHeaderAndCookiePipelines() throws Exception {
        stubTable("男");
        var configs = mock(SpiderConfigService.class);
        var items = mock(SpiderItemService.class);
        var service = new SpiderServiceImpl(configs, items, cookies, execution, headers, null);
        when(configs.getById("batch")).thenReturn(new SpiderConfigTO("batch", "", 1, null, false, null, null));

        var fixed = new ValuePipeline();
        fixed.setEnabled(true);
        fixed.setType(Global.ValuePipelineType.FIXED_JSON_VALUE);
        fixed.setFixedJsonValue("[{\"detail\":{\"gender\":\"MALE\"}}]");
        var nested = objects(List.of(field("gender", "$.detail.gender", List.of(replacement("gender")))));
        nested.setSeq(1);
        var fromHeader = new ValuePipeline();
        fromHeader.setEnabled(true);
        fromHeader.setType(Global.ValuePipelineType.FETCH_HEADER);
        fromHeader.setFetchHeaderName("X-Gender");
        var fromCookie = new ValuePipeline();
        fromCookie.setEnabled(true);
        fromCookie.setType(Global.ValuePipelineType.FETCH_COOKIE);
        fromCookie.setFetchCookieName("gender");

        var setting = new SpiderItemSetting();
        setting.setMode(Global.ExtractionRuleMode.JSON_PATH);
        setting.setUrlType(Global.SpiderUrlType.BY_PARAMS);
        setting.setExtractionRuleList(List.of(rule("rows", "$", List.of(fixed, nested)),
                rule("header", "$", List.of(fromHeader)), rule("cookie", "$", List.of(fromCookie))));
        when(items.getBySpiderId("batch")).thenReturn(List.of(new SpiderItemTO("item", "", setting, null)));
        var header = new Header();
        header.setName("X-Gender");
        header.setValue("MALE");
        header.setValuePipelines(List.of(replacement("gender")));
        var cookie = new Cookie();
        cookie.setName("gender");
        cookie.setValue("MALE");
        cookie.setValuePipelines(List.of(replacement("gender")));
        when(headers.getMapByRefIdsAndType(anyList(), any()))
                .thenReturn(Map.of("item", new HeadersTO("h", List.of(header), "", null)));
        when(cookies.getMapByRefIdsAndType(anyList(), any()))
                .thenReturn(Map.of("item", new CookieListTO("c", List.of(cookie), "", null)));
        var requests = IntStream.range(0, 100).mapToObj(i -> new SpiderReqTO("batch", null, List.of("" + i))).toList();

        var results = service.executeBatch("batch", requests);

        assertEquals(100, results.size());
        for (var json : results) {
            var result = JsonPath.parse(json);
            assertEquals("男", result.read("$.rows[0].gender"));
            assertEquals("男", result.read("$.header"));
            assertEquals("男", result.read("$.cookie"));
        }
        verify(repository).findAllById(Set.of("gender"));
        verifyNoMoreInteractions(repository);

        // 下一批重新載入，不留下跨批次的過期快取。
        stubTable("新值");
        assertEquals("新值", JsonPath.parse(service.executeBatch("batch", requests.subList(0, 1)).getFirst())
                .read("$.rows[0].gender"));
        verify(repository, times(2)).findAllById(Set.of("gender"));
    }

    @Test
    void cssExtractionLoadsNestedTablesBeforeEvaluatingRows() {
        stubTable("男");
        var config = new ExtractObjectsConfig();
        config.setMode(Global.ExtractionRuleMode.SELECT);
        config.setFields(List.of(field("gender", null, List.of(replacement("gender")))));
        var pipeline = objects(List.of());
        pipeline.setExtractObjectsConfig(config);
        var rule = rule("rows", null, List.of(pipeline));
        rule.setSelector("li");
        var result = JsonPath.parse("{}");

        execution.useCssSelect("<ul><li>MALE</li><li>unknown</li></ul>", Map.of(), Map.of(), List.of(rule), result);

        assertEquals(List.of("男"), result.read("$.rows[0].gender"));
        assertEquals(List.of("unknown"), result.read("$.rows[1].gender"));
        verify(repository).findAllById(Set.of("gender"));
    }

    @Test
    void previewLoadsReplacementTables() {
        stubTable("男");
        var testData = new SpiderItemTestData();
        testData.setJson("{\"gender\":\"MALE\"}");
        var setting = new SpiderItemSetting();
        setting.setMode(Global.ExtractionRuleMode.JSON_PATH);
        setting.setTestData(testData);
        setting.setExtractionRuleList(List.of(rule("gender", "$.gender", List.of(replacement("gender")))));
        var service = new SpiderServiceImpl(null, null, cookies, execution, headers, null);

        assertEquals("男", JsonPath.parse(service.previewExtraction(new SpiderItemTO("item", "", setting, null)))
                .read("$.gender"));
        verify(repository).findAllById(Set.of("gender"));
    }

    @Test
    void paginationSharesOneSnapshotAcrossPages() throws Exception {
        stubTable("男");
        var configs = mock(SpiderPaginationService.class);
        var data = mock(SpiderPaginationDataService.class);
        var parameters = mock(SpiderPaginationParameterService.class);
        when(parameters.expand(any(), any())).thenReturn(List.of(Map.of()));
        when(data.updateAll(anyList())).thenAnswer(call -> call.getArgument(0));
        var config = new SpiderPaginationConfig();
        config.setGroupIdTemplate("group");
        config.setDataIdKey("id");
        config.setStartUrl("https://example.test/1");
        config.setNextPageUrlKey("next");
        config.setExtractionRuleMode(Global.ExtractionRuleMode.JSON_PATH);
        config.setExtractionRuleList(List.of(rule("id", "$.id", List.of()),
                rule("next", "$.next", List.of()), rule("gender", "$.gender", List.of(replacement("gender")))));
        when(configs.getById("pages")).thenReturn(new com.lsb.listProjectBackend.domain.spider.SpiderPaginationTO(
                "pages", config, null, null));
        var requests = spy(execution);
        var first = mock(org.jsoup.Connection.class, RETURNS_SELF);
        var second = mock(org.jsoup.Connection.class, RETURNS_SELF);
        when(first.request()).thenReturn(mock(org.jsoup.Connection.Request.class));
        when(second.request()).thenReturn(mock(org.jsoup.Connection.Request.class));
        when(first.get()).thenReturn(org.jsoup.Jsoup.parse(
                "{\"id\":\"1\",\"gender\":\"MALE\",\"next\":\"https://example.test/2\"}"));
        when(second.get()).thenReturn(org.jsoup.Jsoup.parse("{\"id\":\"2\",\"gender\":\"MALE\"}"));
        doReturn(first).when(requests).getConnection("https://example.test/1");
        doReturn(second).when(requests).getConnection("https://example.test/2");
        var service = new SpiderPaginationExecutionService(configs, data, parameters, requests, headers, cookies,
                new SpiderPaginationUpdatePolicy());

        var results = service.execute("pages");

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(row -> "男".equals(row.data().get("gender"))));
        var order = inOrder(repository, first, second);
        order.verify(repository).findAllById(Set.of("gender"));
        order.verify(first).get();
        order.verify(second).get();
        verifyNoMoreInteractions(repository);
    }
}
