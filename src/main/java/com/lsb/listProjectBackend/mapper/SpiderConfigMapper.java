package com.lsb.listProjectBackend.mapper;

import com.lsb.listProjectBackend.domain.SpiderConfigTO;
import com.lsb.listProjectBackend.entity.dynamic.SpiderConfig;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SpiderConfigMapper {
    SpiderConfigMapper INSTANCE = Mappers.getMapper(SpiderConfigMapper.class);

    SpiderConfig toEntity(SpiderConfigTO to);

    List<SpiderConfig> toEntityList(List<SpiderConfigTO> list);

    SpiderConfigTO toDomain(SpiderConfig entity);

    List<SpiderConfigTO> toDomainList(List<SpiderConfig> list);
}
