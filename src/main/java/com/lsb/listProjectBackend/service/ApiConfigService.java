package com.lsb.listProjectBackend.service;

import com.lsb.listProjectBackend.domain.ApiConfigTO;
import com.lsb.listProjectBackend.entity.ApiConfig;
import com.lsb.listProjectBackend.entity.ApiConfigPK;
import com.lsb.listProjectBackend.mapper.ApiConfigMapper;
import com.lsb.listProjectBackend.repository.ApiConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ApiConfigService {
    @Autowired
    private ApiConfigRepository apiConfigRepository;

    private final ApiConfigMapper apiConfigMapper = ApiConfigMapper.INSTANCE;

    public List<ApiConfigTO> getAll() {
        return apiConfigMapper.toDomainList(apiConfigRepository.findAll());
    }

    public List<ApiConfigTO> getListById(List<ApiConfigPK> pkList) {
        return apiConfigMapper.toDomainList(apiConfigRepository.findAllById(pkList));
    }

    public ApiConfigTO getById(ApiConfigPK pk) {
        return apiConfigMapper.toDomain(
                apiConfigRepository.findById(pk).orElse(null)
        );
    }

    public void update(ApiConfigTO apiConfigTO) {
        ApiConfig apiConfig = apiConfigMapper.toEntity(apiConfigTO);
        apiConfig.setUpdatedTime(new Date().getTime());
        this.apiConfigRepository.save(apiConfig);
    }

    public void delete(ApiConfigTO apiConfigTO) {
        this.apiConfigRepository.deleteById(new ApiConfigPK(apiConfigTO.getApiName(), apiConfigTO.getApiLabel()));
    }
}
