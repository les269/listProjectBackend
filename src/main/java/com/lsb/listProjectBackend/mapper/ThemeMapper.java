package com.lsb.listProjectBackend.mapper;

import com.lsb.listProjectBackend.domain.*;
import com.lsb.listProjectBackend.entity.dynamic.ThemeCustomValue;
import com.lsb.listProjectBackend.entity.dynamic.ThemeHeader;
import com.lsb.listProjectBackend.entity.dynamic.ThemeTopCustomValue;

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
