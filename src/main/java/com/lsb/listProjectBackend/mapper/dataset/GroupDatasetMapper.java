package com.lsb.listProjectBackend.mapper.dataset;

import com.lsb.listProjectBackend.domain.dataset.GroupDatasetTO;
import com.lsb.listProjectBackend.entity.dynamic.dataset.GroupDataset;

import com.lsb.listProjectBackend.mapper.SpringAndIgnoreUnmappedMapperConfig;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringAndIgnoreUnmappedMapperConfig.class)
public interface GroupDatasetMapper {

    GroupDataset toEntity(GroupDatasetTO to);

    List<GroupDataset> toEntityList(List<GroupDatasetTO> to);

    GroupDatasetTO toDomain(GroupDataset entity);

    List<GroupDatasetTO> toDomainList(List<GroupDataset> entity);
}
