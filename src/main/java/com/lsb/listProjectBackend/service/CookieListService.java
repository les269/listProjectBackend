package com.lsb.listProjectBackend.service;

import com.lsb.listProjectBackend.domain.CookieListTO;
import com.lsb.listProjectBackend.utils.Global;

import java.util.List;
import java.util.Map;

public interface CookieListService {

    CookieListTO getByRefIdAndType(String refId, Global.CookieListMapType type);

    List<CookieListTO> getAll();

    CookieListTO getByCookieId(String cookieId);

    void update(CookieListTO req);

    void delete(String cookieId);

    Map<String, CookieListTO> getMapByRefIdsAndType(List<String> refIds, Global.CookieListMapType type);

}