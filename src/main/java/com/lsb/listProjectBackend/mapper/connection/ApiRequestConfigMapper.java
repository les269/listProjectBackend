package com.lsb.listProjectBackend.mapper.connection;

import com.lsb.listProjectBackend.domain.connection.ApiRequestConfigTO;
import com.lsb.listProjectBackend.entity.dynamic.common.ApiRequestConfig;

import com.lsb.listProjectBackend.mapper.SpringAndIgnoreUnmappedMapperConfig;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringAndIgnoreUnmappedMapperConfig.class)
public interface ApiRequestConfigMapper {

    ApiRequestConfig toEntity(ApiRequestConfigTO to);

    List<ApiRequestConfig> toEntityList(List<ApiRequestConfigTO> to);

    ApiRequestConfigTO toDomain(ApiRequestConfig entity);

    List<ApiRequestConfigTO> toDomainList(List<ApiRequestConfig> entity);
}
