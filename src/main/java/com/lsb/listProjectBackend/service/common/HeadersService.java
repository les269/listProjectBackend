package com.lsb.listProjectBackend.service.common;

import com.lsb.listProjectBackend.domain.common.HeadersTO;
import com.lsb.listProjectBackend.utils.Global;

import java.util.List;
import java.util.Map;

public interface HeadersService {

    HeadersTO getByRefIdAndType(String refId, Global.HeadersMapType type);

    List<HeadersTO> getAll();

    HeadersTO getByHeadersId(String headersId);

    void update(HeadersTO req);

    void delete(String headersId);

    Map<String, HeadersTO> getMapByRefIdsAndType(List<String> refIds, Global.HeadersMapType type);
}
