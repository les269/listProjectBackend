package com.lsb.listProjectBackend.mapper;

import com.lsb.listProjectBackend.domain.DatabaseConfigTO;
import com.lsb.listProjectBackend.entity.local.DatabaseConfig;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface DatabaseConfigMapper {
    DatabaseConfigMapper INSTANCE = Mappers.getMapper(DatabaseConfigMapper.class);

    DatabaseConfig toEntity(DatabaseConfigTO to);

    List<DatabaseConfig> toEntityList(List<DatabaseConfigTO> to);

    DatabaseConfigTO toDomain(DatabaseConfig entity);

    List<DatabaseConfigTO> toDomainList(List<DatabaseConfig> entity);
}
