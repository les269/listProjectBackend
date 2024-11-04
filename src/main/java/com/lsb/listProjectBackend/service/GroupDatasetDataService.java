package com.lsb.listProjectBackend.service;

import com.lsb.listProjectBackend.domain.GroupDatasetDataTO;
import com.lsb.listProjectBackend.domain.GroupDatasetTO;
import com.lsb.listProjectBackend.entity.GroupDatasetDataPK;
import com.lsb.listProjectBackend.mapper.GroupDatasetDataMapper;
import com.lsb.listProjectBackend.repository.GroupDatasetDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupDatasetDataService {
    @Autowired
    private GroupDatasetDataRepository groupDatasetDataRepository;

    private final GroupDatasetDataMapper groupDatasetDataMapper = GroupDatasetDataMapper.INSTANCE;

    public GroupDatasetDataTO getGroupDatasetData(String groupName, String primeValue) {
        var pk = new GroupDatasetDataPK(groupName, primeValue);
        return groupDatasetDataMapper.toDomain(groupDatasetDataRepository.findById(pk).orElse(null));
    }

    public boolean existGroupDatasetData(String groupName, String primeValue) {
        var pk = new GroupDatasetDataPK(groupName, primeValue);
        return groupDatasetDataRepository.existsById(pk);
    }

    public List<GroupDatasetDataTO> getAllGroupDatasetData(String groupName) {
        return groupDatasetDataMapper.toDomainList(groupDatasetDataRepository.findByGroupName(groupName));
    }

    public void updateGroupDatasetData(GroupDatasetDataTO req) {
        groupDatasetDataRepository.save(groupDatasetDataMapper.toEntity(req));
    }

    public void deleteGroupDatasetData(String groupName, String primeValue) {
        var pk = new GroupDatasetDataPK(groupName, primeValue);
        groupDatasetDataRepository.deleteById(pk);
    }
}
