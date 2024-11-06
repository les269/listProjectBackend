package com.lsb.listProjectBackend.service;

import com.lsb.listProjectBackend.domain.GroupDatasetDataTO;
import com.lsb.listProjectBackend.domain.GroupDatasetTO;
import com.lsb.listProjectBackend.entity.GroupDatasetApi;
import com.lsb.listProjectBackend.entity.GroupDatasetData;
import com.lsb.listProjectBackend.mapper.GroupDatasetDataMapper;
import com.lsb.listProjectBackend.mapper.GroupDatasetMapper;
import com.lsb.listProjectBackend.repository.GroupDatasetDataRepository;
import com.lsb.listProjectBackend.repository.GroupDatasetRepository;
import com.lsb.listProjectBackend.utils.Global;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GroupDatasetService {
    @Autowired
    private GroupDatasetRepository groupDatasetRepository;
    @Autowired
    private ApiConfigService apiConfigService;
    @Autowired
    private GroupDatasetDataRepository groupDatasetDataRepository;

    private final GroupDatasetMapper groupDatasetMapper = GroupDatasetMapper.INSTANCE;

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
                                .toList()
                );
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
                        System.out.println("API call failed for config: " + apiConfig.getApiName() + " - " + e.getMessage());
                    }
                }
            }
        }
    }
}
