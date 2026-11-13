package com.lsb.listProjectBackend.service.dataset;

import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.domain.dataset.GroupDatasetTO;
import com.lsb.listProjectBackend.domain.connection.SingalCallApiReqTO;
import com.lsb.listProjectBackend.entity.dynamic.dataset.GroupDatasetApi;
import com.lsb.listProjectBackend.entity.dynamic.dataset.GroupDatasetData;
import com.lsb.listProjectBackend.mapper.dataset.GroupDatasetMapper;
import com.lsb.listProjectBackend.repository.dynamic.dataset.GroupDatasetDataRepository;
import com.lsb.listProjectBackend.repository.dynamic.dataset.GroupDatasetRepository;
import com.lsb.listProjectBackend.service.connection.ApiRequestConfigService;
import com.lsb.listProjectBackend.utils.Global;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.type.TypeReference;

@Slf4j
@UseDynamic
@Service
@RequiredArgsConstructor
public class GroupDatasetServiceImpl implements GroupDatasetService {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final GroupDatasetRepository groupDatasetRepository;
    private final ApiRequestConfigService apiRequestConfigService;
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
                var apiRequestConfigList = apiRequestConfigService.getListById(
                        config.getGroupDatasetApiList().stream()
                                .map(GroupDatasetApi::getApiId)
                                .toList());
                var pendingRows = new ArrayList<GroupDatasetData>();
                for (var apiRequestConfig : apiRequestConfigList) {
                    try {
                        List<Map<String, Object>> apiResponse = datasetRows(apiRequestConfigService.singalCallApi(
                                new SingalCallApiReqTO(apiRequestConfig.apiId(), objectMapper.createObjectNode())));
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
                            pendingRows.addAll(saveList);
                        }
                    } catch (Exception e) {
                        System.out.println(
                                "API call failed for config: " + apiRequestConfig.apiId() + " - " + e.getMessage());
                    }
                }
                if (!pendingRows.isEmpty()) {
                    groupDatasetDataRepository.saveAll(pendingRows);
                }
            }
        }
    }

    private List<Map<String, Object>> datasetRows(JsonNode response) {
        if (response == null || response.isNull()) {
            return List.of();
        }
        if (response.isObject()) {
            return List.of(objectMapper.convertValue(response, new TypeReference<Map<String, Object>>() {}));
        }
        if (response.isArray()) {
            for (var item : response) {
                if (!item.isObject()) {
                    throw new IllegalArgumentException("Dataset API response must contain only JSON objects");
                }
            }
            return objectMapper.convertValue(response, new TypeReference<List<Map<String, Object>>>() {});
        }
        throw new IllegalArgumentException("Dataset API response must be a JSON object or an array of objects");
    }
}
