package com.lsb.listProjectBackend.mapper.spider;

import com.lsb.listProjectBackend.domain.spider.SpiderPaginationDataTO;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderPaginationData;
import com.lsb.listProjectBackend.mapper.SpringAndIgnoreUnmappedMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringAndIgnoreUnmappedMapperConfig.class)
public interface SpiderPaginationDataMapper {
    SpiderPaginationData toEntity(SpiderPaginationDataTO to);

    List<SpiderPaginationData> toEntityList(List<SpiderPaginationDataTO> list);

    SpiderPaginationDataTO toDomain(SpiderPaginationData entity);

    List<SpiderPaginationDataTO> toDomainList(List<SpiderPaginationData> list);
}
