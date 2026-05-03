package com.lsb.listProjectBackend.mapper.common;

import com.lsb.listProjectBackend.domain.common.CookieListMapTO;
import com.lsb.listProjectBackend.entity.dynamic.common.CookieListMap;

import com.lsb.listProjectBackend.mapper.SpringAndIgnoreUnmappedMapperConfig;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringAndIgnoreUnmappedMapperConfig.class)
public interface CookieListMapMapper {

    CookieListMap toEntity(CookieListMapTO to);

    List<CookieListMap> toEntityList(List<CookieListMapTO> list);

    CookieListMapTO toDomain(CookieListMap entity);

    List<CookieListMapTO> toDomainList(List<CookieListMap> list);
}
