package com.lsb.listProjectBackend.mapper;

import com.lsb.listProjectBackend.domain.ScrapyConfigTO;
import com.lsb.listProjectBackend.entity.dynamic.ScrapyConfig;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ScrapyConfigMapper {
    ScrapyConfigMapper INSTANCE = Mappers.getMapper(ScrapyConfigMapper.class);

    ScrapyConfig toEntity(ScrapyConfigTO to);

    List<ScrapyConfig> toEntityList(List<ScrapyConfigTO> to);

    ScrapyConfigTO toDomain(ScrapyConfig entity);

    List<ScrapyConfigTO> toDomainList(List<ScrapyConfig> entity);
}
