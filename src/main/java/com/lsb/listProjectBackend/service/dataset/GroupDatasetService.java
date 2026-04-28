package com.lsb.listProjectBackend.service.dataset;

import com.lsb.listProjectBackend.domain.dataset.GroupDatasetTO;

import java.util.List;

public interface GroupDatasetService {
    GroupDatasetTO getGroupDataset(String groupName);
    boolean existGroupDataset(String groupName);
    List<GroupDatasetTO> getAllGroupDataset();
    void updateGroupDataset(GroupDatasetTO req);
    void deleteGroupDataset(String groupName);
    void refreshGroupDataset(String groupName);
}
