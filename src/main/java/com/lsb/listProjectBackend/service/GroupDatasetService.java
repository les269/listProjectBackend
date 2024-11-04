package com.lsb.listProjectBackend.service;

import com.lsb.listProjectBackend.domain.GroupDatasetTO;
import com.lsb.listProjectBackend.mapper.GroupDatasetDataMapper;
import com.lsb.listProjectBackend.mapper.GroupDatasetMapper;
import com.lsb.listProjectBackend.repository.GroupDatasetDataRepository;
import com.lsb.listProjectBackend.repository.GroupDatasetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupDatasetService {
    @Autowired
    private GroupDatasetRepository groupDatasetRepository;

    private final GroupDatasetMapper groupDatasetMapper = GroupDatasetMapper.INSTANCE;

    public GroupDatasetTO getGroupDataset(String groupName) {
        return groupDatasetMapper.toDomain(groupDatasetRepository.findById(groupName).orElse(null));
    }

    public boolean existGroupDataset(String groupName) {
        return groupDatasetRepository.existsById(groupName);
    }

    public List<GroupDatasetTO> getAllGroupDataset(){
        return groupDatasetMapper.toDomainList(groupDatasetRepository.findAll());
    }

    public void updateGroupDataset(GroupDatasetTO req){
        groupDatasetRepository.save(groupDatasetMapper.toEntity(req));
    }

    public void deleteGroupDataset(String groupName){
        groupDatasetRepository.deleteById(groupName);
    }
}
