package com.lsb.listProjectBackend.controller;

import com.lsb.listProjectBackend.domain.GroupDatasetDataTO;
import com.lsb.listProjectBackend.service.GroupDatasetDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
public class GroupDatasetDataController {
    @Autowired
    private GroupDatasetDataService groupDatasetDataService;

    @GetMapping("/group-dataset-data/get")
    GroupDatasetDataTO getGroupDatasetData(
            @RequestParam("groupName") String groupName,
            @RequestParam("primeValue") String primeValue) {
        return groupDatasetDataService.getGroupDatasetData(groupName, primeValue);
    }

    @GetMapping("/group-dataset-data/exist")
    boolean existGroupDatasetData(
            @RequestParam("groupName") String groupName,
            @RequestParam("primeValue") String primeValue) {
        return groupDatasetDataService.existGroupDatasetData(groupName, primeValue);
    }

    @GetMapping("/group-dataset-data/all")
    List<GroupDatasetDataTO> getAllGroupDatasetData(
            @RequestParam("groupName") String groupName) {
        return groupDatasetDataService.getAllGroupDatasetData(groupName);
    }

    @PostMapping("/group-dataset-data/update")
    void updateGroupDatasetData(@RequestBody GroupDatasetDataTO req) {
        groupDatasetDataService.updateGroupDatasetData(req);
    }


    @DeleteMapping("/group-dataset-data/delete")
    void deleteGroupDatasetData(
            @RequestParam("groupName") String groupName,
            @RequestParam("primeValue") String primeValue) {
        groupDatasetDataService.deleteGroupDatasetData(groupName, primeValue);
    }
}
