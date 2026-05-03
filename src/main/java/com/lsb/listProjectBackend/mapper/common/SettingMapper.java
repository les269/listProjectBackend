package com.lsb.listProjectBackend.mapper.common;

import com.lsb.listProjectBackend.domain.common.SettingTO;
import com.lsb.listProjectBackend.entity.local.Setting;

import com.lsb.listProjectBackend.mapper.SpringAndIgnoreUnmappedMapperConfig;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringAndIgnoreUnmappedMapperConfig.class)
public interface SettingMapper {

    Setting toEntity(SettingTO to);

    List<Setting> toEntityList(List<SettingTO> to);

    SettingTO toDomain(Setting entity);

    List<SettingTO> toDomainList(List<Setting> entity);
}
