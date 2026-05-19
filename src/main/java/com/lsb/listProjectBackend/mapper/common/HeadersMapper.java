package com.lsb.listProjectBackend.mapper.common;

import com.lsb.listProjectBackend.domain.common.HeadersTO;
import com.lsb.listProjectBackend.entity.dynamic.common.Headers;
import com.lsb.listProjectBackend.mapper.SpringAndIgnoreUnmappedMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringAndIgnoreUnmappedMapperConfig.class)
public interface HeadersMapper {

    Headers toEntity(HeadersTO to);

    List<Headers> toEntityList(List<HeadersTO> list);

    HeadersTO toDomain(Headers entity);

    List<HeadersTO> toDomainList(List<Headers> list);
}
