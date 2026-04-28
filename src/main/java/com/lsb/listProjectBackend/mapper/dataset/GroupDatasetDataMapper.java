package com.lsb.listProjectBackend.mapper.dataset;

import com.lsb.listProjectBackend.domain.dataset.GroupDatasetDataTO;
import com.lsb.listProjectBackend.entity.dynamic.dataset.GroupDatasetData;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface GroupDatasetDataMapper {
    GroupDatasetDataMapper INSTANCE = Mappers.getMapper(GroupDatasetDataMapper.class);

    GroupDatasetData toEntity(GroupDatasetDataTO to);

    List<GroupDatasetData> toEntityList(List<GroupDatasetDataTO> to);

    GroupDatasetData toEntityCopy(GroupDatasetData entity);

    List<GroupDatasetData> toCopyEntityList(List<GroupDatasetData> entity);

    GroupDatasetDataTO toDomain(GroupDatasetData entity);

    List<GroupDatasetDataTO> toDomainList(List<GroupDatasetData> entity);
}
