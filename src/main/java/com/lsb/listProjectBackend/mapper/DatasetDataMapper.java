package com.lsb.listProjectBackend.mapper;

import com.lsb.listProjectBackend.domain.DatasetDataTO;
import com.lsb.listProjectBackend.entity.DatasetData;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface DatasetDataMapper {
    DatasetDataMapper INSTANCE = Mappers.getMapper(DatasetDataMapper.class);

    DatasetData toEntity(DatasetDataTO to);
    List<DatasetData> toEntityList(List<DatasetDataTO> to);
    DatasetDataTO toDomain(DatasetData entity);
    List<DatasetDataTO> toDomainList(List<DatasetData> entity);
}
