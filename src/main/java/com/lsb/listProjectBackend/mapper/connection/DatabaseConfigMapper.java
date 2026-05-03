package com.lsb.listProjectBackend.mapper.connection;

import com.lsb.listProjectBackend.domain.connection.DatabaseConfigTO;
import com.lsb.listProjectBackend.entity.local.DatabaseConfig;
import com.lsb.listProjectBackend.mapper.SpringAndIgnoreUnmappedMapperConfig;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringAndIgnoreUnmappedMapperConfig.class)
public interface DatabaseConfigMapper {

    DatabaseConfig toEntity(DatabaseConfigTO to);

    List<DatabaseConfig> toEntityList(List<DatabaseConfigTO> to);

    DatabaseConfigTO toDomain(DatabaseConfig entity);

    List<DatabaseConfigTO> toDomainList(List<DatabaseConfig> entity);
}
