package com.lsb.listProjectBackend.mapper.common;

import com.lsb.listProjectBackend.domain.common.ReplaceValueMapTO;
import com.lsb.listProjectBackend.entity.dynamic.common.ReplaceValueMap;

import com.lsb.listProjectBackend.mapper.SpringAndIgnoreUnmappedMapperConfig;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringAndIgnoreUnmappedMapperConfig.class)
public interface ReplaceValueMapMapper {

    ReplaceValueMap toEntity(ReplaceValueMapTO to);

    List<ReplaceValueMap> toEntityList(List<ReplaceValueMapTO> to);

    ReplaceValueMapTO toDomain(ReplaceValueMap entity);

    List<ReplaceValueMapTO> toDomainList(List<ReplaceValueMap> entity);
}
