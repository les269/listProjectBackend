package com.lsb.listProjectBackend.controller.dataset;

import com.lsb.listProjectBackend.domain.dataset.GroupDatasetTO;
import com.lsb.listProjectBackend.service.dataset.GroupDatasetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class GroupDatasetController {
    private final GroupDatasetService groupDatasetService;

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
