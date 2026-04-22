package com.lsb.listProjectBackend.mapper;

import com.lsb.listProjectBackend.domain.CookieListMapTO;
import com.lsb.listProjectBackend.entity.dynamic.CookieListMap;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface CookieListMapMapper {
    CookieListMapMapper INSTANCE = Mappers.getMapper(CookieListMapMapper.class);

    CookieListMap toEntity(CookieListMapTO to);

    List<CookieListMap> toEntityList(List<CookieListMapTO> list);

    CookieListMapTO toDomain(CookieListMap entity);

    List<CookieListMapTO> toDomainList(List<CookieListMap> list);
}
