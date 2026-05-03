package com.lsb.listProjectBackend.service.dataset;

import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.domain.dataset.GroupDatasetTO;
import com.lsb.listProjectBackend.entity.dynamic.dataset.GroupDatasetApi;
import com.lsb.listProjectBackend.entity.dynamic.dataset.GroupDatasetData;
import com.lsb.listProjectBackend.mapper.dataset.GroupDatasetMapper;
import com.lsb.listProjectBackend.repository.dynamic.dataset.GroupDatasetDataRepository;
import com.lsb.listProjectBackend.repository.dynamic.dataset.GroupDatasetRepository;
import com.lsb.listProjectBackend.service.connection.ApiConfigService;
import com.lsb.listProjectBackend.service.dataset.GroupDatasetService;
import com.lsb.listProjectBackend.utils.Global;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@UseDynamic
@Service
@RequiredArgsConstructor
public class GroupDatasetServiceImpl implements GroupDatasetService {
    private final GroupDatasetRepository groupDatasetRepository;
    private final ApiConfigService apiConfigService;
    private final GroupDatasetDataRepository groupDatasetDataRepository;

    private final GroupDatasetMapper groupDatasetMapper;

    public GroupDatasetTO getGroupDataset(String groupName) {
        return groupDatasetMapper.toDomain(groupDatasetRepository.findById(groupName).orElse(null));
    }

    public boolean existGroupDataset(String groupName) {
        return groupDatasetRepository.existsById(groupName);
    }

    public List<GroupDatasetTO> getAllGroupDataset() {
        return groupDatasetMapper.toDomainList(groupDatasetRepository.findAll());
    }

    public void updateGroupDataset(GroupDatasetTO req) {
        groupDatasetRepository.save(groupDatasetMapper.toEntity(req));
    }

    public void deleteGroupDataset(String groupName) {
        groupDatasetRepository.deleteById(groupName);
    }

    public void refreshGroupDataset(String groupName) {
        var optional = groupDatasetRepository.findById(groupName);
        if (optional.isPresent()) {
            var groupDataset = optional.get();
            var byKey = groupDataset.getConfig().getByKey();
            var config = groupDataset.getConfig();
            if (config.getType() == Global.GroupDatasetConfigType.api) {
                var apiConfigList = apiConfigService.getByNameList(
                        config.getGroupDatasetApiList().stream()
                                .map(GroupDatasetApi::getApiName)
                                .toList());
                for (var apiConfig : apiConfigList) {
                    try {
                        List<Map<String, Object>> apiResponse = apiConfigService.callApi(apiConfig);
                        if (apiResponse != null) {
                            var saveList = apiResponse.stream()
                                    .filter(data -> data.containsKey(byKey))
                                    .map(data -> {
                                        var groupDatasetData = new GroupDatasetData();
                                        groupDatasetData.setGroupName(groupName);
                                        groupDatasetData.setPrimeValue((String) data.get(byKey));
                                        groupDatasetData.setJson(data);
                                        return groupDatasetData;
                                    }).toList();
                            groupDatasetDataRepository.saveAll(saveList);
                        }
                    } catch (Exception e) {
                        System.out.println(
                                "API call failed for config: " + apiConfig.getApiName() + " - " + e.getMessage());
                    }
                }
            }
        }
    }
}
