package com.lsb.listProjectBackend.mapper.theme;

import com.lsb.listProjectBackend.domain.theme.*;
import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeCustomValue;
import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeHeader;
import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeTopCustomValue;
import com.lsb.listProjectBackend.mapper.SpringAndIgnoreUnmappedMapperConfig;

import org.mapstruct.*;

import java.util.List;

@Mapper(config = SpringAndIgnoreUnmappedMapperConfig.class)
public interface ThemeMapper {

    ThemeHeader headerToEntity(ThemeHeaderTO to);

    ThemeHeaderTO headerToDomain(ThemeHeader entity);

    List<ThemeHeaderTO> headerToDomainList(List<ThemeHeader> entity);

    ThemeCustomValue customValueToEntity(ThemeCustomValueTO to);

    ThemeTopCustomValue topCustomValueToEntity(ThemeTopCustomValueTO to);

}
