package com.lsb.listProjectBackend.service.common;

import com.lsb.listProjectBackend.domain.common.HeadersMapTO;
import com.lsb.listProjectBackend.utils.Global;

import java.util.List;

public interface HeadersMapService {

    HeadersMapTO getMapByIdAndType(String refId, Global.HeadersMapType type);

    List<HeadersMapTO> getMapByRefId(String refId);

    List<HeadersMapTO> getMapByType(Global.HeadersMapType type);

    void updateMap(HeadersMapTO req);

    void deleteMap(String refId, Global.HeadersMapType type);

    boolean isInUseMap(String headersId, Global.HeadersMapType type);

    boolean headersIsInUse(String headersId);

    List<HeadersMapTO> getByRefIdsAndType(List<String> refIds, Global.HeadersMapType type);
}
