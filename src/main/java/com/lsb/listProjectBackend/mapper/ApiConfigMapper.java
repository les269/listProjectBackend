package com.lsb.listProjectBackend.mapper;

import com.lsb.listProjectBackend.domain.ApiConfigTO;
import com.lsb.listProjectBackend.domain.ThemeHeaderTO;
import com.lsb.listProjectBackend.entity.ApiConfig;
import com.lsb.listProjectBackend.entity.ApiConfigPK;
import com.lsb.listProjectBackend.entity.ThemeHeader;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ApiConfigMapper {
    ApiConfigMapper INSTANCE = Mappers.getMapper(ApiConfigMapper.class);

    ApiConfig toEntity(ApiConfigTO to);
    List<ApiConfig> toEntityList(List<ApiConfigTO> to);
    ApiConfigTO toDomain(ApiConfig entity);
    List<ApiConfigTO> toDomainList(List<ApiConfig> entity);
}
