package com.lsb.listProjectBackend.mapper.theme;

import com.lsb.listProjectBackend.domain.theme.ThemeTagValueTO;
import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeTagValue;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ThemeTagValueMapper {
    ThemeTagValueMapper INSTANCE = Mappers.getMapper(ThemeTagValueMapper.class);

    ThemeTagValue toEntity(ThemeTagValueTO to);

    List<ThemeTagValue> toEntityList(List<ThemeTagValueTO> to);

    ThemeTagValueTO toDomain(ThemeTagValue entity);

    List<ThemeTagValueTO> toDomainList(List<ThemeTagValue> entity);
}
