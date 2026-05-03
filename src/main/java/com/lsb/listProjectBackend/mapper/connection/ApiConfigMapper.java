package com.lsb.listProjectBackend.mapper.connection;

import com.lsb.listProjectBackend.domain.connection.ApiConfigTO;
import com.lsb.listProjectBackend.entity.dynamic.common.ApiConfig;

import com.lsb.listProjectBackend.mapper.SpringAndIgnoreUnmappedMapperConfig;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringAndIgnoreUnmappedMapperConfig.class)
public interface ApiConfigMapper {

    ApiConfig toEntity(ApiConfigTO to);

    List<ApiConfig> toEntityList(List<ApiConfigTO> to);

    ApiConfigTO toDomain(ApiConfig entity);

    List<ApiConfigTO> toDomainList(List<ApiConfig> entity);
}
