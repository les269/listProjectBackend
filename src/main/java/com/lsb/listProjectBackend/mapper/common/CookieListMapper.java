package com.lsb.listProjectBackend.mapper.common;

import com.lsb.listProjectBackend.domain.common.CookieListTO;
import com.lsb.listProjectBackend.entity.dynamic.common.CookieList;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface CookieListMapper {
    CookieListMapper INSTANCE = Mappers.getMapper(CookieListMapper.class);

    CookieList toEntity(CookieListTO to);

    List<CookieList> toEntityList(List<CookieListTO> list);

    CookieListTO toDomain(CookieList entity);

    List<CookieListTO> toDomainList(List<CookieList> list);
}
