package com.lsb.listProjectBackend.mapper.dataset;

import com.lsb.listProjectBackend.domain.dataset.GroupDatasetTO;
import com.lsb.listProjectBackend.entity.dynamic.dataset.GroupDataset;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface GroupDatasetMapper {
    GroupDatasetMapper INSTANCE = Mappers.getMapper(GroupDatasetMapper.class);

    GroupDataset toEntity(GroupDatasetTO to);

    List<GroupDataset> toEntityList(List<GroupDatasetTO> to);

    GroupDatasetTO toDomain(GroupDataset entity);

    List<GroupDatasetTO> toDomainList(List<GroupDataset> entity);
}
