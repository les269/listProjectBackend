package com.lsb.listProjectBackend.mapper.dataset;

import com.lsb.listProjectBackend.domain.dataset.DatasetDataTO;
import com.lsb.listProjectBackend.entity.dynamic.dataset.DatasetData;

import com.lsb.listProjectBackend.mapper.SpringAndIgnoreUnmappedMapperConfig;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringAndIgnoreUnmappedMapperConfig.class)
public interface DatasetDataMapper {

    DatasetData toEntity(DatasetDataTO to);

    List<DatasetData> toEntityList(List<DatasetDataTO> to);

    DatasetDataTO toDomain(DatasetData entity);

    List<DatasetDataTO> toDomainList(List<DatasetData> entity);
}
