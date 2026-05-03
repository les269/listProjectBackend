package com.lsb.listProjectBackend.mapper.common;

import com.lsb.listProjectBackend.domain.common.CookieListTO;
import com.lsb.listProjectBackend.entity.dynamic.common.CookieList;

import com.lsb.listProjectBackend.mapper.SpringAndIgnoreUnmappedMapperConfig;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringAndIgnoreUnmappedMapperConfig.class)
public interface CookieListMapper {

    CookieList toEntity(CookieListTO to);

    List<CookieList> toEntityList(List<CookieListTO> list);

    CookieListTO toDomain(CookieList entity);

    List<CookieListTO> toDomainList(List<CookieList> list);
}
