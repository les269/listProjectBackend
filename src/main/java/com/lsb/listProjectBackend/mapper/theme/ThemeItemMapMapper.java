package com.lsb.listProjectBackend.mapper.theme;

import java.util.List;

import com.lsb.listProjectBackend.mapper.SpringAndIgnoreUnmappedMapperConfig;

import org.mapstruct.Mapper;

import com.lsb.listProjectBackend.domain.theme.ThemeItemMapTO;
import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeItemMap;

@Mapper(config = SpringAndIgnoreUnmappedMapperConfig.class)
public interface ThemeItemMapMapper {

    ThemeItemMap toEntity(ThemeItemMapTO to);

    List<ThemeItemMap> toEntity(List<ThemeItemMapTO> list);

    ThemeItemMapTO toDomain(ThemeItemMap entity);

    List<ThemeItemMapTO> toDomain(List<ThemeItemMap> list);

}
