package com.lsb.listProjectBackend.mapper.spider;

import com.lsb.listProjectBackend.domain.spider.SpiderItemTO;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderItem;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SpiderItemMapper {
    SpiderItemMapper INSTANCE = Mappers.getMapper(SpiderItemMapper.class);

    SpiderItem toEntity(SpiderItemTO to);

    List<SpiderItem> toEntityList(List<SpiderItemTO> list);

    SpiderItemTO toDomain(SpiderItem entity);

    List<SpiderItemTO> toDomainList(List<SpiderItem> list);
}
