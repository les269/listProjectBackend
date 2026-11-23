package com.lsb.listProjectBackend.controller.dataset;

import com.lsb.listProjectBackend.domain.dataset.DatasetDataTO;
import com.lsb.listProjectBackend.domain.dataset.DatasetQuickRefreshTO;
import com.lsb.listProjectBackend.domain.dataset.DatasetTO;
import com.lsb.listProjectBackend.service.dataset.DatasetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/dataset")
@RequiredArgsConstructor
public class DatasetController {
    private final DatasetService datasetService;

    @GetMapping("/get")
    DatasetTO getDataset(@RequestParam("name") String name) {
        return datasetService.getDataset(name);
    }

    @GetMapping("/get-data")
    DatasetDataTO getDatasetDataByName(@RequestParam("name") String name) {
        return datasetService.getDatasetDataByName(name);
    }

    @GetMapping("/exist")
    boolean existDataset(@RequestParam("name") String name) {
        return datasetService.existDataset(name);
    }

    @GetMapping("/all")
    List<DatasetTO> getAllDataset() {
        return datasetService.getAllDataset();
    }

    @PostMapping("/update")
    void updateDataset(@RequestBody DatasetTO req) {
        datasetService.updateDataset(req);
    }

    @DeleteMapping("/delete")
    void deleteDataset(@RequestParam("name") String name) {
        datasetService.deleteDataset(name);
    }

    @GetMapping("/refresh")
    void refreshData(@RequestParam("name") String name) throws Exception {
        datasetService.refreshData(name);
    }

    @PostMapping("/name-list/refresh")
    void refreshDataByNameList(@RequestBody List<String> nameList) throws Exception {
        for (var name : nameList.stream().distinct().toList()) {
            datasetService.refreshData(name);
        }
    }

    @PostMapping("/name-list/get-data")
    List<DatasetDataTO> getDatasetDataByNameList(@RequestBody List<String> nameList)  {
        return datasetService.getDatasetDataByNameList(nameList.stream().distinct().toList());
    }

    @PostMapping("/quick-refresh")
    Map<String,Object> quickRefresh(@RequestBody DatasetQuickRefreshTO to) throws Exception {
        return datasetService.quickRefresh(to);
    }
}
