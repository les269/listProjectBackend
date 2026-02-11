package com.lsb.listProjectBackend.service.impl;

import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.domain.GroupDatasetDataTO;
import com.lsb.listProjectBackend.domain.GroupDatasetTO;
import com.lsb.listProjectBackend.entity.dynamic.GroupDatasetDataPK;
import com.lsb.listProjectBackend.mapper.GroupDatasetDataMapper;
import com.lsb.listProjectBackend.repository.dynamic.GroupDatasetDataRepository;
import com.lsb.listProjectBackend.service.GroupDatasetDataService;
import com.lsb.listProjectBackend.service.GroupDatasetService;
import com.lsb.listProjectBackend.utils.Utils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@UseDynamic
@Service
public class GroupDatasetDataServiceImpl implements GroupDatasetDataService {
    @Autowired
    private GroupDatasetDataRepository groupDatasetDataRepository;
    @Autowired
    private GroupDatasetService groupDatasetService;

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

    @Override
    public List<GroupDatasetDataTO> getAllGroupDatasetDataOnlyPrimeValue(String groupName) {
        return groupDatasetDataMapper
                .toDomainList(groupDatasetDataRepository.getAllGroupDatasetDataOnlyPrimeValue(groupName));
    }

    public void updateGroupDatasetData(GroupDatasetDataTO req) {
        groupDatasetDataRepository.save(groupDatasetDataMapper.toEntity(req));
    }

    public void updateGroupDatasetDataList(List<GroupDatasetDataTO> req) {
        groupDatasetDataRepository.saveAll(groupDatasetDataMapper.toEntityList(req));
    }

    public void deleteGroupDatasetData(String groupName, String primeValue) {
        var pk = new GroupDatasetDataPK(groupName, primeValue);
        groupDatasetDataRepository.deleteById(pk);
    }

    public String deleteGroupDatasetDataForImage(String groupName, String primeValue) throws IOException {
        GroupDatasetTO groupDataset = groupDatasetService.getGroupDataset(groupName);
        if (groupDataset != null && Utils.isNotBlank(groupDataset.getConfig().getImageSaveFolder())) {
            String folder = groupDataset.getConfig().getImageSaveFolder();
            String path = folder + "\\" + primeValue;
            if (new File(path).exists()) {
                Utils.deleteFile(path);
                return path;
            } else {
                var image = Arrays.stream(Objects.requireNonNull(
                        new File(folder).listFiles(file -> file.getName().startsWith(primeValue + "."))))
                        .findFirst();
                if (image.isPresent()) {
                    Utils.deleteFile(image.get().getPath());
                    return image.get().getPath();
                }
            }
        }
        return "";
    }
}
