package com.lsb.listProjectBackend.mapper.dataset;

import com.lsb.listProjectBackend.domain.dataset.DatasetTO;
import com.lsb.listProjectBackend.entity.dynamic.dataset.Dataset;

import com.lsb.listProjectBackend.mapper.SpringAndIgnoreUnmappedMapperConfig;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringAndIgnoreUnmappedMapperConfig.class)
public interface DatasetMapper {

    Dataset toEntity(DatasetTO to);

    List<Dataset> toEntityList(List<DatasetTO> to);

    DatasetTO toDomain(Dataset entity);

    List<DatasetTO> toDomainList(List<Dataset> entity);
}
