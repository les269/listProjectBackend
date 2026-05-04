package com.lsb.listProjectBackend.service.dataset;

import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.domain.dataset.GroupDatasetDataTO;
import com.lsb.listProjectBackend.domain.dataset.GroupDatasetTO;
import com.lsb.listProjectBackend.entity.dynamic.dataset.GroupDatasetDataPK;
import com.lsb.listProjectBackend.mapper.dataset.GroupDatasetDataMapper;
import com.lsb.listProjectBackend.repository.dynamic.dataset.GroupDatasetDataRepository;
import com.lsb.listProjectBackend.service.dataset.GroupDatasetDataService;
import com.lsb.listProjectBackend.service.dataset.GroupDatasetService;
import com.lsb.listProjectBackend.utils.Utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@UseDynamic
@Service
@RequiredArgsConstructor
public class GroupDatasetDataServiceImpl implements GroupDatasetDataService {
    private final GroupDatasetDataRepository groupDatasetDataRepository;
    private final GroupDatasetService groupDatasetService;

    private final GroupDatasetDataMapper groupDatasetDataMapper;

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
        if (groupDataset != null && Utils.isNotBlank(groupDataset.config().getImageSaveFolder())) {
            String folder = groupDataset.config().getImageSaveFolder();
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
