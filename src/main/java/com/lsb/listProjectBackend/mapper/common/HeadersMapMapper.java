package com.lsb.listProjectBackend.mapper.common;

import com.lsb.listProjectBackend.domain.common.HeadersMapTO;
import com.lsb.listProjectBackend.entity.dynamic.common.HeadersMap;
import com.lsb.listProjectBackend.mapper.SpringAndIgnoreUnmappedMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringAndIgnoreUnmappedMapperConfig.class)
public interface HeadersMapMapper {

    HeadersMap toEntity(HeadersMapTO to);

    List<HeadersMap> toEntityList(List<HeadersMapTO> list);

    HeadersMapTO toDomain(HeadersMap entity);

    List<HeadersMapTO> toDomainList(List<HeadersMap> list);
}
