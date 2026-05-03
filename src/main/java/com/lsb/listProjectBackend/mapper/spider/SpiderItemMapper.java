package com.lsb.listProjectBackend.mapper.spider;

import com.lsb.listProjectBackend.domain.spider.SpiderItemTO;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderItem;

import com.lsb.listProjectBackend.mapper.SpringAndIgnoreUnmappedMapperConfig;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringAndIgnoreUnmappedMapperConfig.class)
public interface SpiderItemMapper {

    SpiderItem toEntity(SpiderItemTO to);

    List<SpiderItem> toEntityList(List<SpiderItemTO> list);

    SpiderItemTO toDomain(SpiderItem entity);

    List<SpiderItemTO> toDomainList(List<SpiderItem> list);
}
