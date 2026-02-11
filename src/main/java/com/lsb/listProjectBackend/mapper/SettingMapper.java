package com.lsb.listProjectBackend.mapper;

import com.lsb.listProjectBackend.domain.SettingTO;
import com.lsb.listProjectBackend.entity.local.Setting;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SettingMapper {
    SettingMapper INSTANCE = Mappers.getMapper(SettingMapper.class);

    Setting toEntity(SettingTO to);

    List<Setting> toEntityList(List<SettingTO> to);

    SettingTO toDomain(Setting entity);

    List<SettingTO> toDomainList(List<Setting> entity);
}
