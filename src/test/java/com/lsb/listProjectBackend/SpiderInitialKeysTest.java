package com.lsb.listProjectBackend;

import com.jayway.jsonpath.JsonPath;
import com.lsb.listProjectBackend.domain.spider.SpiderConfigTO;
import com.lsb.listProjectBackend.domain.spider.SpiderItemTO;
import com.lsb.listProjectBackend.entity.dynamic.spider.ExtractionRule;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderItemSetting;
import com.lsb.listProjectBackend.entity.dynamic.spider.ValuePipeline;
import com.lsb.listProjectBackend.service.common.CookieListService;
import com.lsb.listProjectBackend.service.common.HeadersService;
import com.lsb.listProjectBackend.service.spider.*;
import com.lsb.listProjectBackend.utils.Global;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SpiderInitialKeysTest {
    @ParameterizedTest
    @EnumSource(Global.ExtractionRuleMode.class)
    void initialKeysRemainAvailableAfterEmptyInitializationAndHttpRequest(Global.ExtractionRuleMode mode)
            throws Exception {
        var configs = mock(SpiderConfigService.class);
        var items = mock(SpiderItemService.class);
        var cookies = mock(CookieListService.class);
        var headers = mock(HeadersService.class);
        var execution = spy(new SpiderExecutionService(cookies, new ExtractionRuleServiceImpl(), headers,
                    new ReplaceValueMapLoader(mock(com.lsb.listProjectBackend.repository.dynamic.common.ReplaceValueMapRepository.class))));
        var service = new SpiderServiceImpl(configs, items, cookies, execution, headers, null);
        when(configs.getById("spider"))
                .thenReturn(new SpiderConfigTO("spider", "", 1, null, false, null, 15L));
        when(cookies.getMapByRefIdsAndType(anyList(), any())).thenReturn(Map.of());
        when(headers.getMapByRefIdsAndType(anyList(), any())).thenReturn(Map.of());
        when(items.getBySpiderId("spider")).thenReturn(List.of(
                item("initialize", "", mode),
                item("fetch", "https://example.test/{{$.initialize[0]}}", mode)));
        var connection = mock(Connection.class);
        var request = mock(Connection.Request.class);
        doReturn(connection).when(execution).getConnection("https://example.test/123");
        when(connection.request()).thenReturn(request);
        when(request.cookies()).thenReturn(Map.of());
        when(request.headers()).thenReturn(Map.of());
        when(connection.ignoreContentType(true)).thenReturn(connection);
        when(connection.get()).thenReturn(mode == Global.ExtractionRuleMode.SELECT
                ? Jsoup.parse("<p>remote</p>") : Jsoup.parse("{}"));

        String result = service.executeByPrimeKeyList("spider", List.of("123", "news"));

        assertEquals(List.of("123", "news"), JsonPath.read(result, "$.initialize"));
        assertEquals(List.of("123", "news"), JsonPath.read(result, "$.fetch"));
        verify(execution).getConnection("https://example.test/123");
        verify(execution).waitForRequestInterval(null);
    }

    private SpiderItemTO item(String key, String url, Global.ExtractionRuleMode mode) {
        var pipeline = new ValuePipeline();
        pipeline.setEnabled(true);
        pipeline.setType(Global.ValuePipelineType.INITIAL_KEYS);
        var rule = new ExtractionRule();
        rule.setKey(key);
        rule.setJsonPath("$.missing");
        rule.setPipelines(List.of(pipeline));
        var setting = new SpiderItemSetting();
        setting.setUrl(url);
        setting.setUrlType(Global.SpiderUrlType.BY_PARAMS);
        setting.setMode(mode);
        setting.setExtractionRuleList(List.of(rule));
        return new SpiderItemTO(key, "", setting, null);
    }
}
