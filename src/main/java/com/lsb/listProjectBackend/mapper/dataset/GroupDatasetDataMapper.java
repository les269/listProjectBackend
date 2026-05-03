package com.lsb.listProjectBackend.mapper.dataset;

import com.lsb.listProjectBackend.domain.dataset.GroupDatasetDataTO;
import com.lsb.listProjectBackend.entity.dynamic.dataset.GroupDatasetData;

import com.lsb.listProjectBackend.mapper.SpringAndIgnoreUnmappedMapperConfig;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringAndIgnoreUnmappedMapperConfig.class)
public interface GroupDatasetDataMapper {

    GroupDatasetData toEntity(GroupDatasetDataTO to);

    List<GroupDatasetData> toEntityList(List<GroupDatasetDataTO> to);

    GroupDatasetData toEntityCopy(GroupDatasetData entity);

    List<GroupDatasetData> toCopyEntityList(List<GroupDatasetData> entity);

    GroupDatasetDataTO toDomain(GroupDatasetData entity);

    List<GroupDatasetDataTO> toDomainList(List<GroupDatasetData> entity);
}
