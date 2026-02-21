package com.lsb.listProjectBackend.service;

import com.lsb.listProjectBackend.domain.DatasetDataTO;
import com.lsb.listProjectBackend.domain.DatasetQuickRefreshTO;
import com.lsb.listProjectBackend.domain.DatasetTO;

import java.util.List;
import java.util.Map;

public interface DatasetService {
    List<DatasetTO> getAllDataset();
    void updateDataset(DatasetTO req);
    void deleteDataset(String name);
    DatasetTO getDataset(String name);
    DatasetDataTO getDatasetDataByName(String name);
    List<DatasetDataTO> getDatasetDataByNameList(List<String> nameList);
    boolean existDataset(String name);
    void refreshData(String name) throws Exception;
    Map<String,Object> quickRefresh(DatasetQuickRefreshTO to) throws Exception;
}
