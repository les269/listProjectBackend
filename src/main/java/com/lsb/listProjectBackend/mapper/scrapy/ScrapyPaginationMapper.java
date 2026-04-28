package com.lsb.listProjectBackend.mapper.scrapy;

import com.lsb.listProjectBackend.domain.scrapy.ScrapyPaginationTO;
import com.lsb.listProjectBackend.entity.dynamic.scrapy.ScrapyPagination;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ScrapyPaginationMapper {
    ScrapyPaginationMapper INSTANCE = Mappers.getMapper(ScrapyPaginationMapper.class);

    ScrapyPagination toEntity(ScrapyPaginationTO to);

    List<ScrapyPagination> toEntityList(List<ScrapyPaginationTO> to);

    ScrapyPaginationTO toDomain(ScrapyPagination entity);

    List<ScrapyPaginationTO> toDomainList(List<ScrapyPagination> entity);
}
