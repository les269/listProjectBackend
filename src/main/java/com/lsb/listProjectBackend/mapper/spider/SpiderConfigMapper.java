package com.lsb.listProjectBackend.mapper.spider;

import com.lsb.listProjectBackend.domain.spider.SpiderConfigTO;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderConfig;

import com.lsb.listProjectBackend.mapper.SpringAndIgnoreUnmappedMapperConfig;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringAndIgnoreUnmappedMapperConfig.class)
public interface SpiderConfigMapper {

    SpiderConfig toEntity(SpiderConfigTO to);

    List<SpiderConfig> toEntityList(List<SpiderConfigTO> list);

    SpiderConfigTO toDomain(SpiderConfig entity);

    List<SpiderConfigTO> toDomainList(List<SpiderConfig> list);
}
