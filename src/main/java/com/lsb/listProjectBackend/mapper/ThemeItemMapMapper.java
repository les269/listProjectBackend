package com.lsb.listProjectBackend.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.lsb.listProjectBackend.domain.ThemeItemMapTO;
import com.lsb.listProjectBackend.entity.dynamic.ThemeItemMap;

@Mapper
public interface ThemeItemMapMapper {
    ThemeItemMapMapper INSTANCE = Mappers.getMapper(ThemeItemMapMapper.class);

    ThemeItemMap toEntity(ThemeItemMapTO to);

    List<ThemeItemMap> toEntity(List<ThemeItemMapTO> list);

    ThemeItemMapTO toDomain(ThemeItemMap entity);

    List<ThemeItemMapTO> toDomain(List<ThemeItemMap> list);

}
