package com.lsb.listProjectBackend.mapper;

import com.lsb.listProjectBackend.domain.ThemeHiddenTO;
import com.lsb.listProjectBackend.entity.dynamic.ThemeHidden;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ThemeHiddenMapper {
    ThemeHiddenMapper INSTANCE = Mappers.getMapper(ThemeHiddenMapper.class);

    ThemeHidden toEntity(ThemeHiddenTO to);

    ThemeHiddenTO toDomain(ThemeHidden entity);

    List<ThemeHiddenTO> toDomainList(List<ThemeHidden> entity);
}
