package com.lsb.listProjectBackend.mapper.theme;

import com.lsb.listProjectBackend.domain.theme.ThemeTagValueTO;
import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeTagValue;

import com.lsb.listProjectBackend.mapper.SpringAndIgnoreUnmappedMapperConfig;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringAndIgnoreUnmappedMapperConfig.class)
public interface ThemeTagValueMapper {

    ThemeTagValue toEntity(ThemeTagValueTO to);

    List<ThemeTagValue> toEntityList(List<ThemeTagValueTO> to);

    ThemeTagValueTO toDomain(ThemeTagValue entity);

    List<ThemeTagValueTO> toDomainList(List<ThemeTagValue> entity);
}
