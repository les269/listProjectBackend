package com.lsb.listProjectBackend.service;

import com.lsb.listProjectBackend.domain.CookieListMapTO;
import com.lsb.listProjectBackend.utils.Global;

import java.util.List;

public interface CookieListMapService {

    CookieListMapTO getMapByIdAndType(String refId, Global.CookieListMapType type);

    List<CookieListMapTO> getMapByRefId(String refId);

    List<CookieListMapTO> getMapByType(Global.CookieListMapType type);

    void updateMap(CookieListMapTO req);

    void deleteMap(String refId, Global.CookieListMapType type);

    boolean isInUseMap(String cookieId, Global.CookieListMapType type);

    boolean cookieIsInUse(String cookieId);

    List<CookieListMapTO> getByRefIdsAndType(List<String> refIds, Global.CookieListMapType type);
}
