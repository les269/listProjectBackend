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
    private GroupDatasetDataRepository groupDatasetDataRepository;
    @Autowired
    private GroupDatasetRepository groupDatasetRepository;

    private final GroupDatasetMapper groupDatasetMapper = GroupDatasetMapper.INSTANCE;
    private final GroupDatasetDataMapper groupDatasetDataMapper = GroupDatasetDataMapper.INSTANCE;

    public GroupDatasetTO getGroupDataset(String name) {
        return groupDatasetMapper.toDomain(groupDatasetRepository.findById(name).orElse(null));
    }

    public boolean existGroupDataset(String name) {
        return groupDatasetRepository.existsById(name);
    }

    public List<GroupDatasetTO> getAllGroupDataset(){
        return groupDatasetMapper.toDomainList(groupDatasetRepository.findAll());
    }

    public void updateGroupDataset(GroupDatasetTO req){
        groupDatasetRepository.save(groupDatasetMapper.toEntity(req));
    }

    public void deleteGroupDataset(String name){
        groupDatasetRepository.deleteById(name);
    }
}
