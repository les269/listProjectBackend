package com.lsb.listProjectBackend.mapper.theme;

import com.lsb.listProjectBackend.domain.theme.ThemeHiddenTO;
import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeHidden;

import com.lsb.listProjectBackend.mapper.SpringAndIgnoreUnmappedMapperConfig;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringAndIgnoreUnmappedMapperConfig.class)
public interface ThemeHiddenMapper {

    ThemeHidden toEntity(ThemeHiddenTO to);

    ThemeHiddenTO toDomain(ThemeHidden entity);

    List<ThemeHiddenTO> toDomainList(List<ThemeHidden> entity);
}
