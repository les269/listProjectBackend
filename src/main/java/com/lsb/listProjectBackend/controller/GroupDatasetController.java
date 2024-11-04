package com.lsb.listProjectBackend.controller;

import com.lsb.listProjectBackend.domain.GroupDatasetTO;
import com.lsb.listProjectBackend.service.GroupDatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
public class GroupDatasetController {
    @Autowired
    private GroupDatasetService groupDatasetService;

    @GetMapping("/group-dataset/get")
    GroupDatasetTO getGroupDataset(@RequestParam("name") String name) {
        return groupDatasetService.getGroupDataset(name);
    }

    @GetMapping("/group-dataset/exist")
    boolean existGroupDataset(@RequestParam("name") String name) {
        return groupDatasetService.existGroupDataset(name);
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
    void deleteGroupDataset(@RequestParam("name") String name) {
        groupDatasetService.deleteGroupDataset(name);
    }
}
