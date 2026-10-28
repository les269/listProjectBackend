package com.lsb.listProjectBackend.mapper.spider;

import com.lsb.listProjectBackend.domain.spider.SpiderPaginationTO;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderPagination;
import com.lsb.listProjectBackend.mapper.SpringAndIgnoreUnmappedMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringAndIgnoreUnmappedMapperConfig.class)
public interface SpiderPaginationMapper {
    SpiderPagination toEntity(SpiderPaginationTO to);

    List<SpiderPagination> toEntityList(List<SpiderPaginationTO> list);

    SpiderPaginationTO toDomain(SpiderPagination entity);

    List<SpiderPaginationTO> toDomainList(List<SpiderPagination> list);
}
