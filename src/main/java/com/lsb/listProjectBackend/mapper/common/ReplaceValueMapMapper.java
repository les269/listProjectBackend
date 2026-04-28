package com.lsb.listProjectBackend.mapper.common;

import com.lsb.listProjectBackend.domain.common.ReplaceValueMapTO;
import com.lsb.listProjectBackend.entity.dynamic.common.ReplaceValueMap;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ReplaceValueMapMapper {
    ReplaceValueMapMapper INSTANCE = Mappers.getMapper(ReplaceValueMapMapper.class);

    ReplaceValueMap toEntity(ReplaceValueMapTO to);

    List<ReplaceValueMap> toEntityList(List<ReplaceValueMapTO> to);

    ReplaceValueMapTO toDomain(ReplaceValueMap entity);

    List<ReplaceValueMapTO> toDomainList(List<ReplaceValueMap> entity);
}
