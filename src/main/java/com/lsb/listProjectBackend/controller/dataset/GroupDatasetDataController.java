package com.lsb.listProjectBackend.controller.dataset;

import com.lsb.listProjectBackend.domain.dataset.GroupDatasetDataTO;
import com.lsb.listProjectBackend.service.dataset.GroupDatasetDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/group-dataset-data")
@RequiredArgsConstructor
public class GroupDatasetDataController {
    private final GroupDatasetDataService groupDatasetDataService;

    @GetMapping("/get")
    GroupDatasetDataTO getGroupDatasetData(
            @RequestParam("groupName") String groupName,
            @RequestParam("primeValue") String primeValue) {
        return groupDatasetDataService.getGroupDatasetData(groupName, primeValue);
    }

    @GetMapping("/exist")
    boolean existGroupDatasetData(
            @RequestParam("groupName") String groupName,
            @RequestParam("primeValue") String primeValue) {
        return groupDatasetDataService.existGroupDatasetData(groupName, primeValue);
    }

    @GetMapping("/all")
    List<GroupDatasetDataTO> getAllGroupDatasetData(
            @RequestParam("groupName") String groupName) {
        return groupDatasetDataService.getAllGroupDatasetData(groupName);
    }

    @GetMapping("/all-only-prime-value")
    List<GroupDatasetDataTO> getAllGroupDatasetDataNoJson(
            @RequestParam("groupName") String groupName) {
        return groupDatasetDataService.getAllGroupDatasetDataOnlyPrimeValue(groupName);
    }

    @PostMapping("/update")
    void updateGroupDatasetData(@RequestBody GroupDatasetDataTO req) {
        groupDatasetDataService.updateGroupDatasetData(req);
    }

    @DeleteMapping("/delete")
    void deleteGroupDatasetData(
            @RequestParam("groupName") String groupName,
            @RequestParam("primeValue") String primeValue) {
        groupDatasetDataService.deleteGroupDatasetData(groupName, primeValue);
    }

    @DeleteMapping("/delete-image")
    String deleteGroupDatasetDataForImage(
            @RequestParam("groupName") String groupName,
            @RequestParam("primeValue") String primeValue) throws IOException {
        return groupDatasetDataService.deleteGroupDatasetDataForImage(groupName, primeValue);
    }

    @PostMapping("/update-list")
    void updateGroupDatasetDataList(@RequestBody List<GroupDatasetDataTO> req) {
        groupDatasetDataService.updateGroupDatasetDataList(req);
    }
}
