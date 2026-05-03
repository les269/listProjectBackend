package com.lsb.listProjectBackend.mapper.scrapy;

import com.lsb.listProjectBackend.domain.scrapy.ScrapyPaginationTO;
import com.lsb.listProjectBackend.entity.dynamic.scrapy.ScrapyPagination;

import com.lsb.listProjectBackend.mapper.SpringAndIgnoreUnmappedMapperConfig;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringAndIgnoreUnmappedMapperConfig.class)
public interface ScrapyPaginationMapper {

    ScrapyPagination toEntity(ScrapyPaginationTO to);

    List<ScrapyPagination> toEntityList(List<ScrapyPaginationTO> to);

    ScrapyPaginationTO toDomain(ScrapyPagination entity);

    List<ScrapyPaginationTO> toDomainList(List<ScrapyPagination> entity);
}
