package com.lsb.listProjectBackend.controller;

import com.lsb.listProjectBackend.domain.GroupDatasetTO;
import com.lsb.listProjectBackend.service.GroupDatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
public class GroupDatasetController {
    @Autowired
    private GroupDatasetService groupDatasetService;

    @GetMapping("/group-dataset/get")
    GroupDatasetTO getGroupDataset(@RequestParam("groupName") String groupName) {
        return groupDatasetService.getGroupDataset(groupName);
    }

    @GetMapping("/group-dataset/exist")
    boolean existGroupDataset(@RequestParam("groupName") String groupName) {
        return groupDatasetService.existGroupDataset(groupName);
    }

    @GetMapping("/group-dataset/all")
    List<GroupDatasetTO> getAllGroupDataset() {
        return groupDatasetService.getAllGroupDataset();
    }

    @PostMapping("/group-dataset/update")
    void updateGroupDataset(@RequestBody GroupDatasetTO req) {
        groupDatasetService.updateGroupDataset(req);
    }


    @DeleteMapping("/group-dataset/delete")
    void deleteGroupDataset(@RequestParam("groupName") String groupName) {
        groupDatasetService.deleteGroupDataset(groupName);
    }
    @GetMapping("/group-dataset/refresh")
    void refreshGroupDataset(@RequestParam("groupName") String groupName) {
        groupDatasetService.refreshGroupDataset(groupName);
    }
}
