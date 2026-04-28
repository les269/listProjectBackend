package com.lsb.listProjectBackend.mapper.spider;

import com.lsb.listProjectBackend.domain.spider.SpiderMappingTO;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderMapping;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SpiderMappingMapper {
    SpiderMappingMapper INSTANCE = Mappers.getMapper(SpiderMappingMapper.class);

    SpiderMapping toEntity(SpiderMappingTO to);

    List<SpiderMapping> toEntityList(List<SpiderMappingTO> list);

    SpiderMappingTO toDomain(SpiderMapping entity);

    List<SpiderMappingTO> toDomainList(List<SpiderMapping> list);
}
