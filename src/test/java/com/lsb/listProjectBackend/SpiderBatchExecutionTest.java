package com.lsb.listProjectBackend;

import com.lsb.listProjectBackend.domain.spider.*;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderItemSetting;
import com.lsb.listProjectBackend.service.common.CookieListService;
import com.lsb.listProjectBackend.service.common.HeadersService;
import com.lsb.listProjectBackend.service.spider.*;
import com.lsb.listProjectBackend.utils.Global;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SpiderBatchExecutionTest {
    private final SpiderConfigService configs = mock(SpiderConfigService.class);
    private final SpiderItemService items = mock(SpiderItemService.class);
    private final CookieListService cookies = mock(CookieListService.class);
    private final HeadersService headers = mock(HeadersService.class);
    private final SpiderExecutionService execution = mock(SpiderExecutionService.class);
    private final SpiderServiceImpl service = new SpiderServiceImpl(configs, items, cookies, execution, headers, null);

    @BeforeEach
    void setup() {
        when(configs.getById("detail")).thenReturn(new SpiderConfigTO("detail", "", 1, null, false, null, 15L));
        var setting = new SpiderItemSetting();
        setting.setUrl("https://example.test/{{$[0]}}");
        setting.setUrlType(Global.SpiderUrlType.BY_PRIME_KEY);
        setting.setMode(Global.ExtractionRuleMode.SELECT);
        setting.setExtractionRuleList(List.of());
        when(items.getBySpiderId("detail")).thenReturn(List.of(new SpiderItemTO("item", "", setting, null)));
        when(cookies.getMapByRefIdsAndType(anyList(), any())).thenReturn(Map.of());
        when(headers.getMapByRefIdsAndType(anyList(), any())).thenReturn(Map.of());
    }

    @Test
    void loadsAllDatabaseSettingsBeforeExecutingMultipleUrls() throws Exception {
        var results = service.executeBatch("detail", List.of(
                new SpiderReqTO("detail", "https://example.test/one", null),
                new SpiderReqTO("detail", "https://example.test/two", null)));

        assertEquals(List.of("{}", "{}"), results);
        var order = inOrder(configs, items, cookies, headers, execution);
        order.verify(configs).getById("detail");
        order.verify(items).getBySpiderId("detail");
        order.verify(cookies).getMapByRefIdsAndType(List.of("item"), Global.CookieListMapType.SPIDER);
        order.verify(headers).getMapByRefIdsAndType(List.of("item"), Global.HeadersMapType.SPIDER);
        order.verify(execution).loadReplaceValueMaps(anyList(), anyList(), anyList());
        order.verify(execution).executeRequest(eq("https://example.test/one"), any(), anyList(), isNull(),
                isNull(), any(), isNull(), anyList(), anyMap());
        order.verify(execution).waitForRequestInterval(15L);
        order.verify(execution).executeRequest(eq("https://example.test/two"), any(), anyList(), isNull(),
                isNull(), any(), isNull(), anyList(), anyMap());
        verifyNoMoreInteractions(configs, items, cookies, headers, execution);
    }

    @Test
    void substitutesPrimeKeysForEveryRequestWithoutReloadingSettings() throws Exception {
        service.executeBatch("detail", List.of(new SpiderReqTO("detail", null, List.of("one")),
                new SpiderReqTO("detail", null, List.of("two"))));
        verify(execution).executeRequest(eq("https://example.test/one"), any(), anyList(), isNull(),
                isNull(), any(), isNull(), eq(List.of("one")), anyMap());
        verify(execution).executeRequest(eq("https://example.test/two"), any(), anyList(), isNull(),
                isNull(), any(), isNull(), eq(List.of("two")), anyMap());
        verify(configs).getById("detail");
        verify(items).getBySpiderId("detail");
        verifyNoMoreInteractions(configs, items);
    }

    @Test
    void failedUrlReturnsAnEmptyResultAndDoesNotShiftLaterBatchResults() throws Exception {
        doThrow(new IllegalArgumentException("Malformed URL"))
                .when(execution).executeRequest(eq("{{$.workno}}?locale=zh_TW"), any(), anyList(),
                        any(), any(), any(), any(), anyList(), anyMap());
        doAnswer(invocation -> {
            com.jayway.jsonpath.DocumentContext result = invocation.getArgument(5);
            result.put("$", "title", "success");
            return null;
        }).when(execution).executeRequest(eq("https://example.test/two"), any(), anyList(),
                any(), any(), any(), any(), anyList(), anyMap());

        var results = service.executeBatch("detail", List.of(
                new SpiderReqTO("detail", "{{$.workno}}?locale=zh_TW", null),
                new SpiderReqTO("detail", "https://example.test/two", null)));

        assertEquals(List.of("{}", "{\"title\":\"success\"}"), results);
    }

    @Test
    void rejectsPrimeKeysForUrlOnlySpider() {
        when(configs.getById("detail")).thenReturn(new SpiderConfigTO("detail", "", 1, null, true, null, null));
        assertThrows(com.lsb.listProjectBackend.domain.common.LsbException.class,
                () -> service.executeBatch("detail", List.of(new SpiderReqTO("detail", null, List.of("one")))));
        verifyNoInteractions(items, cookies, headers, execution);
    }

    @Test
    void rejectsMixedBatchForUrlOnlySpiderBeforeExecutingAnyRequest() {
        when(configs.getById("detail")).thenReturn(new SpiderConfigTO("detail", "", 1, null, true, null, null));

        assertThrows(com.lsb.listProjectBackend.domain.common.LsbException.class,
                () -> service.executeBatch("detail", List.of(
                        new SpiderReqTO("detail", "https://example.test/one", null),
                        new SpiderReqTO("detail", "   ", List.of("two")))));

        verifyNoInteractions(items, cookies, headers, execution);
    }

    @Test
    void urlOnlySpiderAcceptsUrlsUsingBatchSpiderId() throws Exception {
        when(configs.getById("detail")).thenReturn(new SpiderConfigTO("detail", "", 1, null, true, null, null));

        var results = service.executeBatch("detail", List.of(
                new SpiderReqTO(null, "https://example.test/one", null),
                new SpiderReqTO("other", "https://example.test/two", null)));

        assertEquals(List.of("{}", "{}"), results);
        verify(configs).getById("detail");
        verify(items).getBySpiderId("detail");
        verify(execution).executeRequest(eq("https://example.test/one"), any(), anyList(), isNull(),
                isNull(), any(), isNull(), anyList(), anyMap());
        verify(execution).executeRequest(eq("https://example.test/two"), any(), anyList(), isNull(),
                isNull(), any(), isNull(), anyList(), anyMap());
    }

    @Test
    void emptyBatchDoesNotLoadSettings() throws Exception {
        assertEquals(List.of(), service.executeBatch("detail", List.of()));
        verifyNoInteractions(configs, items, cookies, headers, execution);
    }
}
