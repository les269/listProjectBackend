package com.lsb.listProjectBackend.controller;

import com.lsb.listProjectBackend.domain.DatasetDataTO;
import com.lsb.listProjectBackend.domain.DatasetQuickRefreshTO;
import com.lsb.listProjectBackend.domain.DatasetTO;
import com.lsb.listProjectBackend.service.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
public class DatasetController {
    @Autowired
    private DatasetService datasetService;

    @GetMapping("/dataset/get")
    DatasetTO getDataset(@RequestParam("name") String name) {
        return datasetService.getDataset(name);
    }

    @GetMapping("/dataset/get-data")
    DatasetDataTO getDatasetDataByName(@RequestParam("name") String name) {
        return datasetService.getDatasetDataByName(name);
    }

    @GetMapping("/dataset/exist")
    boolean existDataset(@RequestParam("name") String name) {
        return datasetService.existDataset(name);
    }

    @GetMapping("/dataset/all")
    List<DatasetTO> getAllDataset() {
        return datasetService.getAllDataset();
    }

    @PostMapping("/dataset/update")
    void updateDataset(@RequestBody DatasetTO req) {
        datasetService.updateDataset(req);
    }

    @DeleteMapping("/dataset/delete")
    void deleteDataset(@RequestParam("name") String name) {
        datasetService.deleteDataset(name);
    }

    @GetMapping("/dataset/refresh")
    void refreshData(@RequestParam("name") String name) throws Exception {
        datasetService.refreshData(name);
    }

    @PostMapping("/dataset/name-list/refresh")
    void refreshDataByNameList(@RequestBody List<String> nameList) throws Exception {
        for (var name : nameList.stream().distinct().toList()) {
            datasetService.refreshData(name);
        }
    }

    @PostMapping("/dataset/name-list/get-data")
    List<DatasetDataTO> getDatasetDataByNameList(@RequestBody List<String> nameList)  {
        return datasetService.getDatasetDataByNameList(nameList.stream().distinct().toList());
    }

    @PostMapping("/dataset/quick-refresh")
    Map<String,Object> quickRefresh(@RequestBody DatasetQuickRefreshTO to) throws Exception {
        return datasetService.quickRefresh(to);
    }
}
