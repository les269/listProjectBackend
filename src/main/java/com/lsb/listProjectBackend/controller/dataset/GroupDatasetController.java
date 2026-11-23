package com.lsb.listProjectBackend.controller.dataset;

import com.lsb.listProjectBackend.domain.dataset.GroupDatasetTO;
import com.lsb.listProjectBackend.service.dataset.GroupDatasetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/group-dataset")
@RequiredArgsConstructor
public class GroupDatasetController {
    private final GroupDatasetService groupDatasetService;

    @GetMapping("/get")
    GroupDatasetTO getGroupDataset(@RequestParam("groupName") String groupName) {
        return groupDatasetService.getGroupDataset(groupName);
    }

    @GetMapping("/exist")
    boolean existGroupDataset(@RequestParam("groupName") String groupName) {
        return groupDatasetService.existGroupDataset(groupName);
    }

    @GetMapping("/all")
    List<GroupDatasetTO> getAllGroupDataset() {
        return groupDatasetService.getAllGroupDataset();
    }

    @PostMapping("/update")
    void updateGroupDataset(@RequestBody GroupDatasetTO req) {
        groupDatasetService.updateGroupDataset(req);
    }


    @DeleteMapping("/delete")
    void deleteGroupDataset(@RequestParam("groupName") String groupName) {
        groupDatasetService.deleteGroupDataset(groupName);
    }
    @GetMapping("/refresh")
    void refreshGroupDataset(@RequestParam("groupName") String groupName) {
        groupDatasetService.refreshGroupDataset(groupName);
    }
}
