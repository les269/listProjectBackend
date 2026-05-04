package com.lsb.listProjectBackend.service.connection;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.domain.connection.ApiConfigTO;
import com.lsb.listProjectBackend.entity.dynamic.common.ApiConfig;
import com.lsb.listProjectBackend.mapper.connection.ApiConfigMapper;
import com.lsb.listProjectBackend.repository.dynamic.connection.ApiConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@UseDynamic
@Service
@RequiredArgsConstructor
public class ApiConfigServiceImpl implements ApiConfigService {
    private final ApiConfigRepository apiConfigRepository;

    private static final RestTemplate restTemplate = new RestTemplate();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ApiConfigMapper apiConfigMapper;

    public List<ApiConfigTO> getAll() {
        return apiConfigMapper.toDomainList(apiConfigRepository.findAll());
    }

    public List<ApiConfigTO> getListById(List<String> nameList) {
        return apiConfigMapper.toDomainList(apiConfigRepository.findAllById(nameList));
    }

    public ApiConfigTO getByName(String name) {
        return apiConfigMapper.toDomain(
                apiConfigRepository.findById(name).orElse(null));
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
        this.apiConfigRepository.deleteById(apiConfigTO.apiName());
    }

    public List<Map<String, Object>> callApi(ApiConfigTO apiConfig) throws Exception {
        try {
            String url = apiConfig.endpointUrl();
            String response = "";
            switch (apiConfig.httpMethod()) {
                case get:
                    response = restTemplate.getForObject(url, String.class);
                    break;
                case post:
                    response = restTemplate.postForObject(url,
                            objectMapper.readValue(apiConfig.requestBody(), new TypeReference<>() {
                            }),
                            String.class);
                    break;
                case put:
                    restTemplate.put(url,
                            objectMapper.readValue(apiConfig.requestBody(), new TypeReference<>() {
                            }),
                            String.class);
                    break;
                case delete:
                    restTemplate.delete(url, String.class);
                    break;
            }
            if (response != null) {
                return objectMapper.readValue(response, new TypeReference<>() {
                });
            } else {
                throw new IllegalArgumentException("Response is null");
            }
        } catch (HttpStatusCodeException e) {
            log.error("HTTP error", e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to call API", e);
            throw e;
        }
    }
}
