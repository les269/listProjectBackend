package com.lsb.listProjectBackend.mapper.file;

import com.lsb.listProjectBackend.domain.file.DiskTO;
import com.lsb.listProjectBackend.entity.local.Disk;
import com.lsb.listProjectBackend.mapper.SpringAndIgnoreUnmappedMapperConfig;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringAndIgnoreUnmappedMapperConfig.class)
public interface DiskMapper {

    Disk toEntity(DiskTO to);

    List<Disk> toEntityList(List<DiskTO> to);

    DiskTO toDomain(Disk entity);

    List<DiskTO> toDomainList(List<Disk> entity);
}
