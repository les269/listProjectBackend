package com.lsb.listProjectBackend.mapper;

import com.lsb.listProjectBackend.domain.DatasetTO;
import com.lsb.listProjectBackend.entity.dynamic.Dataset;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface DatasetMapper {
    DatasetMapper INSTANCE = Mappers.getMapper(DatasetMapper.class);

    Dataset toEntity(DatasetTO to);

    List<Dataset> toEntityList(List<DatasetTO> to);

    DatasetTO toDomain(Dataset entity);

    List<DatasetTO> toDomainList(List<Dataset> entity);
}
