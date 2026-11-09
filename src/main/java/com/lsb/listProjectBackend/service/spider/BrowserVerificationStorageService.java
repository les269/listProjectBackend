package com.lsb.listProjectBackend.service.spider;

import com.lsb.listProjectBackend.aop.UseDynamicTx;
import com.lsb.listProjectBackend.domain.common.*;
import com.lsb.listProjectBackend.domain.spider.BrowserVerificationTO.*;
import com.lsb.listProjectBackend.entity.dynamic.common.Cookie;
import com.lsb.listProjectBackend.entity.dynamic.common.Header;
import com.lsb.listProjectBackend.service.common.*;
import com.lsb.listProjectBackend.utils.Global;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BrowserVerificationStorageService {
    private final CookieListService cookieLists;
    private final HeadersService headers;
    private final CookieListMapService cookieMaps;
    private final HeadersMapService headerMaps;
    private final SpiderItemService items;
    private final SpiderPaginationService pagination;

    /** Keep DB routing and transaction on the request thread, not Playwright's worker. */
    @UseDynamicTx
    public Saved save(CompleteRequest request, BrowserVerificationService.VerifiedSession verified) {
        if (request == null || request.refId() == null || request.refId().isBlank() || request.type() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "refId and type are required");
        }
        boolean exists = request.type() == TargetType.SPIDER
                ? items.exists(request.refId()) : pagination.exists(request.refId());
        if (!exists) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Spider target not found");
        }
        var cookieType = Global.CookieListMapType.valueOf(request.type().name());
        var headerType = Global.HeadersMapType.valueOf(request.type().name());
        var oldCookies = cookieLists.getByRefIdAndType(request.refId(), cookieType);
        var oldHeaders = headers.getByRefIdAndType(request.refId(), headerType);

        // Clone into dedicated lists so refreshing one target cannot change another target's shared lists.
        var mergedCookies = new LinkedHashMap<String, Cookie>();
        if (oldCookies != null && oldCookies.list() != null) {
            oldCookies.list().forEach(cookie -> mergedCookies.put(cookie.getName(), cookie));
        }
        verified.cookies().forEach((name, value) -> {
            Cookie cookie = new Cookie();
            cookie.setName(name);
            cookie.setValue(value);
            cookie.setSeq(mergedCookies.size());
            cookie.setValuePipelines(List.of());
            mergedCookies.put(name, cookie);
        });
        List<Header> mergedHeaders = new ArrayList<>();
        if (oldHeaders != null && oldHeaders.list() != null) {
            oldHeaders.list().stream().filter(header -> !"User-Agent".equalsIgnoreCase(header.getName()))
                    .forEach(mergedHeaders::add);
        }
        Header userAgent = new Header();
        userAgent.setName("User-Agent");
        userAgent.setValue(verified.userAgent());
        userAgent.setSeq(mergedHeaders.size());
        userAgent.setValuePipelines(List.of());
        mergedHeaders.add(userAgent);

        String cookieId = UUID.randomUUID().toString();
        String headersId = UUID.randomUUID().toString();
        cookieLists.update(new CookieListTO(cookieId, new ArrayList<>(mergedCookies.values()),
                "Browser verification: " + request.refId(), null));
        headers.update(new HeadersTO(headersId, mergedHeaders, "Browser verification: " + request.refId(), null));
        cookieMaps.updateMap(new CookieListMapTO(request.refId(), cookieType, cookieId, null));
        headerMaps.updateMap(new HeadersMapTO(request.refId(), headerType, headersId, null));
        return new Saved(cookieId, headersId, verified.status(), verified.title());
    }
}
