package com.lsb.listProjectBackend.service;

import com.lsb.listProjectBackend.domain.ApiConfigTO;

import java.util.List;
import java.util.Map;

public interface ApiConfigService {
    List<ApiConfigTO> getAll();
    List<ApiConfigTO> getListById(List<String> nameList);
    ApiConfigTO getByName(String name);
    List<ApiConfigTO> getByNameList(List<String> nameList);
    void update(ApiConfigTO apiConfigTO);
    void delete(ApiConfigTO apiConfigTO);
    List<Map<String, Object>> callApi(ApiConfigTO apiConfig) throws Exception;
}
