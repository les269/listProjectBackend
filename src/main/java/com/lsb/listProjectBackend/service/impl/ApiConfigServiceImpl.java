package com.lsb.listProjectBackend.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lsb.listProjectBackend.domain.ApiConfigTO;
import com.lsb.listProjectBackend.entity.ApiConfig;
import com.lsb.listProjectBackend.mapper.ApiConfigMapper;
import com.lsb.listProjectBackend.repository.ApiConfigRepository;
import com.lsb.listProjectBackend.service.ApiConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ApiConfigServiceImpl implements ApiConfigService {
    @Autowired
    private ApiConfigRepository apiConfigRepository;

    private static final RestTemplate restTemplate = new RestTemplate();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ApiConfigMapper apiConfigMapper = ApiConfigMapper.INSTANCE;

    public List<ApiConfigTO> getAll() {
        return apiConfigMapper.toDomainList(apiConfigRepository.findAll());
    }

    public List<ApiConfigTO> getListById(List<String> nameList) {
        return apiConfigMapper.toDomainList(apiConfigRepository.findAllById(nameList));
    }

    public ApiConfigTO getByName(String name) {
        return apiConfigMapper.toDomain(
                apiConfigRepository.findById(name).orElse(null)
        );
    }

    public List<ApiConfigTO> getByNameList(List<String> nameList) {
        return apiConfigMapper.toDomainList(apiConfigRepository.findAllById(nameList));
    }

    public void update(ApiConfigTO apiConfigTO) {
        ApiConfig apiConfig = apiConfigMapper.toEntity(apiConfigTO);
        apiConfig.setUpdatedTime(new Date().getTime());
        this.apiConfigRepository.save(apiConfig);
    }

    public void delete(ApiConfigTO apiConfigTO) {
        this.apiConfigRepository.deleteById(apiConfigTO.getApiName());
    }

    public List<Map<String, Object>> callApi(ApiConfigTO apiConfig) throws Exception {
        try {
            String url = apiConfig.getEndpointUrl();
            String response = restTemplate.getForObject(url, String.class);
            if (response != null) {
                return objectMapper.readValue(response, new TypeReference<>() {
                });
            } else {
                throw new IllegalArgumentException("Response is null");
            }
        } catch (HttpStatusCodeException e) {
            System.err.println("HTTP error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            System.err.println("Failed to call API: " + e.getMessage());
            throw e;
        }
    }
}
