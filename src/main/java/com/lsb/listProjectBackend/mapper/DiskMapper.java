package com.lsb.listProjectBackend.mapper;

import com.lsb.listProjectBackend.domain.DiskTO;
import com.lsb.listProjectBackend.entity.local.Disk;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface DiskMapper {
    DiskMapper INSTANCE = Mappers.getMapper(DiskMapper.class);

    Disk toEntity(DiskTO to);

    List<Disk> toEntityList(List<DiskTO> to);

    DiskTO toDomain(Disk entity);

    List<DiskTO> toDomainList(List<Disk> entity);
}
