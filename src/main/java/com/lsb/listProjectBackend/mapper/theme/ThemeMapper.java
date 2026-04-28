package com.lsb.listProjectBackend.mapper.theme;

import com.lsb.listProjectBackend.domain.theme.*;
import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeCustomValue;
import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeHeader;
import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeTopCustomValue;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ThemeMapper {
    ThemeMapper INSTANCE = Mappers.getMapper(ThemeMapper.class);

    ThemeHeader headerToEntity(ThemeHeaderTO to);

    ThemeHeaderTO headerToDomain(ThemeHeader entity);

    List<ThemeHeaderTO> headerToDomainList(List<ThemeHeader> entity);

    ThemeCustomValue customValueToEntity(ThemeCustomValueTO to);

    ThemeTopCustomValue topCustomValueToEntity(ThemeTopCustomValueTO to);

}
