package com.lsb.listProjectBackend.service.connection;

import com.lsb.listProjectBackend.domain.connection.ApiRequestConfigTO;
import com.lsb.listProjectBackend.domain.connection.SingalCallApiReqTO;

import java.util.List;
import tools.jackson.databind.JsonNode;

public interface ApiRequestConfigService {
    boolean isExist(String apiId);
    List<ApiRequestConfigTO> getAll();
    List<ApiRequestConfigTO> getListById(List<String> apiIds);
    ApiRequestConfigTO getById(String apiId);
    void update(ApiRequestConfigTO apiRequestConfigTO);
    void delete(ApiRequestConfigTO apiRequestConfigTO);
    JsonNode singalCallApi(SingalCallApiReqTO request) throws Exception;
}
