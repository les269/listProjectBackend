package com.lsb.listProjectBackend.mapper;

import com.lsb.listProjectBackend.domain.ScrapyConfigTO;
import com.lsb.listProjectBackend.domain.ScrapyPaginationTO;
import com.lsb.listProjectBackend.entity.dynamic.ScrapyConfig;
import com.lsb.listProjectBackend.entity.dynamic.ScrapyPagination;

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
