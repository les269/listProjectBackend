package com.lsb.listProjectBackend.service;

import com.lsb.listProjectBackend.domain.GroupDatasetDataTO;

import java.io.IOException;
import java.util.List;

public interface GroupDatasetDataService {
    GroupDatasetDataTO getGroupDatasetData(String groupName, String primeValue);
    boolean existGroupDatasetData(String groupName, String primeValue);
    List<GroupDatasetDataTO> getAllGroupDatasetData(String groupName);
    List<GroupDatasetDataTO> getAllGroupDatasetDataOnlyPrimeValue(String groupName);
    void updateGroupDatasetData(GroupDatasetDataTO req);
    void updateGroupDatasetDataList(List<GroupDatasetDataTO> req);
    void deleteGroupDatasetData(String groupName, String primeValue);
    String deleteGroupDatasetDataForImage(String groupName, String primeValue) throws IOException;
}
