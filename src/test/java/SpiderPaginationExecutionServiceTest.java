import com.jayway.jsonpath.DocumentContext;
import com.lsb.listProjectBackend.domain.spider.SpiderPaginationDataTO;
import com.lsb.listProjectBackend.domain.spider.SpiderPaginationTO;
import com.lsb.listProjectBackend.entity.dynamic.spider.ParamSetting;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderPaginationConfig;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderPaginationDataPK;
import com.lsb.listProjectBackend.service.common.CookieListService;
import com.lsb.listProjectBackend.service.common.HeadersService;
import com.lsb.listProjectBackend.service.spider.*;
import com.lsb.listProjectBackend.utils.Global;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import com.lsb.listProjectBackend.entity.dynamic.spider.UpdateControl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SpiderPaginationExecutionServiceTest {
    private final SpiderPaginationService configs = mock(SpiderPaginationService.class);
    private final SpiderPaginationDataService data = mock(SpiderPaginationDataService.class);
    private final SpiderExecutionService requests = mock(SpiderExecutionService.class);
    private final Map<List<String>, SpiderPaginationDataTO> stored = new HashMap<>();
    private final SpiderPaginationExecutionService service = new SpiderPaginationExecutionService(configs, data,
            new SpiderPaginationParameterService(), requests, mock(HeadersService.class), mock(CookieListService.class),
            new SpiderPaginationUpdatePolicy());

    private SpiderPaginationConfig configure() {
        var config = new SpiderPaginationConfig();
        config.setGroupIdTemplate("anime-2026");
        config.setDataIdKey("id");
        config.setStartUrl("https://example.test/page/1");
        config.setNextPageUrlKey("next");
        config.setExtractionRuleMode(Global.ExtractionRuleMode.JSON_PATH);
        config.setExtractionRuleList(List.of());
        config.setRequestIntervalMs(100L);
        var parameter = new ParamSetting();
        parameter.setParamKey("year");
        parameter.setType(Global.ParamType.STATIC_LIST);
        parameter.setStaticList(List.of("2026"));
        config.setParamSettings(new java.util.ArrayList<>(List.of(parameter)));
        when(configs.getById("test")).thenReturn(new SpiderPaginationTO("test", config, null, null));
        when(data.getById(anyString(), anyString(), anyString())).thenAnswer(call -> stored.get(
                List.of(call.getArgument(0), call.getArgument(1), call.getArgument(2))));
        when(data.getBySpiderPaginationId(anyString())).thenAnswer(call -> stored.values().stream()
                .filter(record -> record.spiderPaginationId().equals(call.getArgument(0))).toList());
        doAnswer(call -> {
            List<SpiderPaginationDataTO> records = call.getArgument(0);
            records.forEach(record -> stored.put(
                    List.of(record.spiderPaginationId(), record.groupId(), record.dataId()), record));
            return records;
        }).when(data).updateAll(anyList());
        doAnswer(call -> {
            List<SpiderPaginationDataPK> obsoleteIds = call.getArgument(1);
            obsoleteIds.forEach(id -> stored.remove(List.of(id.getSpiderPaginationId(), id.getGroupId(), id.getDataId())));
            List<SpiderPaginationDataTO> records = call.getArgument(0);
            records.forEach(record -> stored.put(
                    List.of(record.spiderPaginationId(), record.groupId(), record.dataId()), record));
            return records;
        }).when(data).synchronize(anyList(), anyList());
        return config;
    }

    private void addOldRecord(String paginationId, String groupId) {
        LocalDateTime time = LocalDateTime.now().minusDays(2);
        stored.put(List.of(paginationId, groupId, "old"), new SpiderPaginationDataTO(
                paginationId, groupId, "old", Map.of("id", "old"), time, time, time));
    }

    @Test
    void omittedOrNullRetainHistoryKeepsOldRecords() throws Exception {
        var config = configure();
        assertTrue(config.isRetainHistory());
        addOldRecord("test", "anime-2026");
        doAnswer(call -> {
            call.<DocumentContext>getArgument(5).put("$", "id", "new");
            return null;
        }).when(requests).executeRequest(anyString(), any(), anyList(), any(), any(), any(), nullable(Long.class));

        service.execute("test");
        assertTrue(stored.containsKey(List.of("test", "anime-2026", "old")));
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        assertTrue(mapper.readValue("{}", SpiderPaginationConfig.class).isRetainHistory());
        assertTrue(mapper.readValue("{\"retainHistory\":null}", SpiderPaginationConfig.class).isRetainHistory());
        assertFalse(mapper.readValue("{\"retainHistory\":false}", SpiderPaginationConfig.class).isRetainHistory());
        service.execute("test");
        assertTrue(stored.containsKey(List.of("test", "anime-2026", "old")));
        verify(data, never()).synchronize(anyList(), anyList());
    }

    @Test
    void synchronizationRemovesOnlyObsoleteRecordsInCompletedGroup() throws Exception {
        var config = configure();
        config.setRetainHistory(false);
        addOldRecord("test", "anime-2026");
        addOldRecord("test", "another-group");
        addOldRecord("another-pagination", "anime-2026");
        doAnswer(call -> {
            verify(data, never()).synchronize(anyList(), anyList());
            call.<DocumentContext>getArgument(5).put("$", "id", "new");
            return null;
        }).when(requests).executeRequest(anyString(), any(), anyList(), any(), any(), any(), nullable(Long.class));

        assertEquals("new", service.execute("test").getFirst().dataId());
        assertFalse(stored.containsKey(List.of("test", "anime-2026", "old")));
        assertTrue(stored.containsKey(List.of("test", "another-group", "old")));
        assertTrue(stored.containsKey(List.of("another-pagination", "anime-2026", "old")));
        verify(data).synchronize(anyList(), eq(List.of(new SpiderPaginationDataPK("test", "anime-2026", "old"))));
        verify(data, never()).updateAll(anyList());
    }

    @Test
    void synchronizationPreservesSkippedGroups() throws Exception {
        var config = configure();
        config.setRetainHistory(false);
        var control = new UpdateControl();
        control.setLastUpdateMode(Global.LastUpdateMode.FIXED);
        control.setLastUpdateDate(LocalDateTime.now().minusDays(1));
        config.setUpdateControl(control);
        addOldRecord("test", "anime-2026");

        assertTrue(service.execute("test").isEmpty());
        assertTrue(stored.containsKey(List.of("test", "anime-2026", "old")));
        verifyNoInteractions(requests);
        verify(data, never()).synchronize(anyList(), anyList());
    }

    @Test
    void synchronizationPreservesOldRecordsWhenLaterPageFails() throws Exception {
        var config = configure();
        config.setRetainHistory(false);
        addOldRecord("test", "anime-2026");
        doAnswer(call -> {
            if (call.<String>getArgument(0).endsWith("/2")) {
                throw new IllegalStateException("request failed");
            }
            var result = call.<DocumentContext>getArgument(5);
            result.put("$", "id", "new");
            result.put("$", "next", "https://example.test/page/2");
            return null;
        }).when(requests).executeRequest(anyString(), any(), anyList(), any(), any(), any(), nullable(Long.class));

        assertThrows(IllegalStateException.class, () -> service.execute("test"));
        assertTrue(stored.containsKey(List.of("test", "anime-2026", "old")));
        assertNull(stored.get(List.of("test", "anime-2026", "new")).lastSuccessTime());
        verify(data).synchronize(anyList(), eq(List.of()));
    }

    @Test
    void collectsPagesAndSavesOnceAfterRequestsFinish() throws Exception {
        configure();
        doAnswer(call -> {
            verify(data, never()).updateAll(anyList());
            verify(data, never()).update(any());
            verify(data, never()).getById(anyString(), anyString(), anyString());
            verify(data, times(1)).getBySpiderPaginationId("test");
            String url = call.getArgument(0);
            DocumentContext result = call.getArgument(5);
            result.put("$", "id", url.endsWith("/1") ? "a" : "b");
            result.put("$", "title", "complete extracted data");
            if (url.endsWith("/1")) result.put("$", "next", "https://example.test/page/2");
            return null;
        }).when(requests).executeRequest(anyString(), any(), anyList(), any(), any(), any(), nullable(Long.class));
        var result = service.execute("test");
        assertEquals(List.of("a", "b"), result.stream().map(SpiderPaginationDataTO::dataId).toList());
        assertEquals("complete extracted data", result.getFirst().data().get("title"));
        assertNotNull(result.getFirst().lastSuccessTime());
        verify(data, times(1)).updateAll(anyList());
        verify(requests).executeRequest(eq("https://example.test/page/1"), any(), anyList(), any(), any(), any(), isNull());
        verify(requests).executeRequest(eq("https://example.test/page/2"), any(), anyList(), any(), any(), any(), eq(100L));
    }

    @Test
    void sameDataIdCanBeStoredInDifferentParameterGroups() throws Exception {
        var config = configure();
        config.setGroupIdTemplate("anime-{{$.year}}");
        var year = new ParamSetting();
        year.setParamKey("year");
        year.setType(Global.ParamType.STATIC_LIST);
        year.setStaticList(List.of("2025", "2026"));
        config.setParamSettings(new java.util.ArrayList<>(List.of(year)));
        doAnswer(call -> {
            call.<DocumentContext>getArgument(5).put("$", "id", "same-id");
            return null;
        }).when(requests).executeRequest(anyString(), any(), anyList(), any(), any(), any(), nullable(Long.class));
        assertEquals(2, service.execute("test").size());
        assertEquals(2, stored.size());
        verify(data, times(1)).getBySpiderPaginationId("test");
        verify(data, times(1)).updateAll(anyList());
        verify(requests).executeRequest(anyString(), any(), anyList(), any(), any(), any(), isNull());
        verify(requests).executeRequest(anyString(), any(), anyList(), any(), any(), any(), eq(100L));
    }

    @Test
    void repeatedUrlFailsWithoutDeletingSuccessfulPages() throws Exception {
        configure();
        doAnswer(call -> {
            var result = call.<DocumentContext>getArgument(5);
            result.put("$", "id", "a");
            result.put("$", "next", "https://example.test/page/1");
            return null;
        }).when(requests).executeRequest(anyString(), any(), anyList(), any(), any(), any(), nullable(Long.class));
        assertThrows(Exception.class, () -> service.execute("test"));
        assertEquals(1, stored.size());
        assertNull(stored.values().iterator().next().lastSuccessTime());
        verify(data, times(1)).updateAll(anyList());
        verify(data, never()).deleteBySpiderPaginationId(anyString());
    }

    @Test
    void missingDataIdDoesNotWriteAnything() throws Exception {
        configure();
        assertThrows(Exception.class, () -> service.execute("test"));
        verify(data, never()).update(any());
        verify(data, never()).updateAll(anyList());
    }

    @Test
    void expiredRecordIsPreservedUnlessForced() throws Exception {
        var config = configure();
        var control = new UpdateControl();
        control.setLastUpdateMode(Global.LastUpdateMode.FIXED);
        control.setLastUpdateDate(LocalDateTime.now().minusDays(1));
        config.setUpdateControl(control);
        var oldTime = LocalDateTime.now().minusDays(2);
        var old = new SpiderPaginationDataTO("test", "anime-2026", "a", Map.of("id", "a", "title", "old"),
                oldTime, oldTime, oldTime);
        stored.put(List.of("test", "anime-2026", "a"), old);
        doAnswer(call -> {
            var result = call.<DocumentContext>getArgument(5);
            result.put("$", "id", "a");
            result.put("$", "title", "new");
            return null;
        }).when(requests).executeRequest(anyString(), any(), anyList(), any(), any(), any(), nullable(Long.class));
        assertTrue(service.execute("test").isEmpty());
        assertEquals(old, stored.get(List.of("test", "anime-2026", "a")));
        verifyNoInteractions(requests);
        var updated = service.execute("test", true).getFirst();
        assertEquals("new", updated.data().get("title"));
        assertEquals(oldTime, updated.createdTime());
        assertTrue(updated.lastSuccessTime().isAfter(oldTime));
    }

    @Test
    void failedGroupRetriesEvenWhenDeadlineHasPassed() throws Exception {
        var config = configure();
        var control = new UpdateControl();
        control.setLastUpdateMode(Global.LastUpdateMode.FIXED);
        control.setLastUpdateDate(LocalDateTime.now().minusDays(1));
        config.setUpdateControl(control);
        var oldTime = LocalDateTime.now().minusDays(2);
        var old = new SpiderPaginationDataTO("test", "anime-2026", "a", Map.of("id", "a"),
                oldTime, oldTime, null);
        stored.put(List.of("test", "anime-2026", "a"), old);
        doAnswer(call -> {
            var result = call.<DocumentContext>getArgument(5);
            boolean first = call.<String>getArgument(0).endsWith("/1");
            result.put("$", "id", first ? "a" : "b");
            if (first) result.put("$", "next", "https://example.test/page/2");
            return null;
        }).when(requests).executeRequest(anyString(), any(), anyList(), any(), any(), any(), nullable(Long.class));
        assertEquals(List.of("a", "b"), service.execute("test").stream().map(SpiderPaginationDataTO::dataId).toList());
        assertNotNull(stored.get(List.of("test", "anime-2026", "a")).lastSuccessTime());
    }

    @Test
    void forceRefreshesAllGroupsForPaginationId() throws Exception {
        var config = configure();
        config.setGroupIdTemplate("anime-{{$.year}}");
        config.getParamSettings().getFirst().setStaticList(List.of("2025", "2026"));
        var control = new UpdateControl();
        control.setLastUpdateMode(Global.LastUpdateMode.FIXED);
        control.setLastUpdateDate(LocalDateTime.now().minusDays(1));
        config.setUpdateControl(control);
        LocalDateTime oldTime = LocalDateTime.now().minusDays(2);
        for (String year : List.of("2025", "2026")) {
            String group = "anime-" + year;
            stored.put(List.of("test", group, "a"), new SpiderPaginationDataTO("test", group, "a",
                    Map.of("id", "a"), oldTime, oldTime, oldTime));
        }
        doAnswer(call -> {
            call.<DocumentContext>getArgument(5).put("$", "id", "a");
            return null;
        }).when(requests).executeRequest(anyString(), any(), anyList(), any(), any(), any(), nullable(Long.class));

        assertTrue(service.execute("test").isEmpty());
        verifyNoInteractions(requests);
        assertEquals(List.of("anime-2025", "anime-2026"), service.execute("test", true).stream()
                .map(SpiderPaginationDataTO::groupId).toList());
        verify(requests, times(2)).executeRequest(anyString(), any(), anyList(), any(), any(), any(), nullable(Long.class));
    }

    @Test
    void separateExecutionsEachStartWithoutWaiting() throws Exception {
        configure();
        doAnswer(call -> {
            call.<DocumentContext>getArgument(5).put("$", "id", "a");
            return null;
        }).when(requests).executeRequest(anyString(), any(), anyList(), any(), any(), any(), nullable(Long.class));
        service.execute("test");
        service.execute("test");
        verify(requests, times(2)).executeRequest(anyString(), any(), anyList(), any(), any(), any(), isNull());
    }
}
