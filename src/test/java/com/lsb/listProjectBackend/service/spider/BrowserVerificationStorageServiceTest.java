package com.lsb.listProjectBackend.service.spider;

import com.lsb.listProjectBackend.domain.common.*;
import com.lsb.listProjectBackend.domain.spider.BrowserVerificationTO.*;
import com.lsb.listProjectBackend.entity.dynamic.common.Cookie;
import com.lsb.listProjectBackend.entity.dynamic.common.Header;
import com.lsb.listProjectBackend.service.common.*;
import com.lsb.listProjectBackend.utils.Global;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BrowserVerificationStorageServiceTest {
    private final CookieListService cookies = mock(CookieListService.class);
    private final HeadersService headers = mock(HeadersService.class);
    private final CookieListMapService cookieMaps = mock(CookieListMapService.class);
    private final HeadersMapService headerMaps = mock(HeadersMapService.class);
    private final SpiderItemService items = mock(SpiderItemService.class);
    private final SpiderPaginationService pagination = mock(SpiderPaginationService.class);
    private final BrowserVerificationStorageService storage = new BrowserVerificationStorageService(
            cookies, headers, cookieMaps, headerMaps, items, pagination);
    private final BrowserVerificationService.VerifiedSession verified =
            new BrowserVerificationService.VerifiedSession(Map.of("cf_clearance", "new-test-value"), "new-agent", 200, "AniDB");

    @Test
    void preservesOtherSettingsWithoutMutatingSharedListsAndMapsPaginationTarget() {
        when(pagination.exists("pages")).thenReturn(true);
        Cookie old = new Cookie();
        old.setName("preference");
        old.setValue("keep");
        when(cookies.getByRefIdAndType("pages", Global.CookieListMapType.SPIDER_PAGINATION))
                .thenReturn(new CookieListTO("shared-cookies", List.of(old), "shared", null));
        Header oldAgent = new Header();
        oldAgent.setName("user-agent");
        oldAgent.setValue("old-agent");
        Header other = new Header();
        other.setName("Accept");
        other.setValue("text/html");
        when(headers.getByRefIdAndType("pages", Global.HeadersMapType.SPIDER_PAGINATION))
                .thenReturn(new HeadersTO("shared-headers", List.of(oldAgent, other), "shared", null));

        var result = storage.save(new CompleteRequest("pages", TargetType.SPIDER_PAGINATION), verified);
        var cookieCapture = ArgumentCaptor.forClass(CookieListTO.class);
        var headerCapture = ArgumentCaptor.forClass(HeadersTO.class);
        verify(cookies).update(cookieCapture.capture());
        verify(headers).update(headerCapture.capture());
        assertNotEquals("shared-cookies", result.cookieId());
        assertNotEquals("shared-headers", result.headersId());
        assertEquals(List.of("preference", "cf_clearance"),
                cookieCapture.getValue().list().stream().map(Cookie::getName).toList());
        assertEquals(List.of("Accept", "User-Agent"),
                headerCapture.getValue().list().stream().map(Header::getName).toList());
        assertEquals("old-agent", oldAgent.getValue());
        verify(cookieMaps).updateMap(new CookieListMapTO("pages", Global.CookieListMapType.SPIDER_PAGINATION, result.cookieId(), null));
        verify(headerMaps).updateMap(new HeadersMapTO("pages", Global.HeadersMapType.SPIDER_PAGINATION, result.headersId(), null));
    }

    @Test
    void invalidOrMissingTargetDoesNotWriteAnything() {
        assertThrows(ResponseStatusException.class, () -> storage.save(new CompleteRequest("", TargetType.SPIDER), verified));
        assertThrows(ResponseStatusException.class, () -> storage.save(new CompleteRequest("missing", TargetType.SPIDER), verified));
        verifyNoInteractions(cookies, headers, cookieMaps, headerMaps);
    }
}
