package com.lsb.listProjectBackend.mapper.scrapy;

import com.lsb.listProjectBackend.domain.scrapy.ScrapyConfigTO;
import com.lsb.listProjectBackend.entity.dynamic.scrapy.ScrapyConfig;

import com.lsb.listProjectBackend.mapper.SpringAndIgnoreUnmappedMapperConfig;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringAndIgnoreUnmappedMapperConfig.class)
public interface ScrapyConfigMapper {

    ScrapyConfig toEntity(ScrapyConfigTO to);

    List<ScrapyConfig> toEntityList(List<ScrapyConfigTO> to);

    ScrapyConfigTO toDomain(ScrapyConfig entity);

    List<ScrapyConfigTO> toDomainList(List<ScrapyConfig> entity);
}
