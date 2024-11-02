package com.lsb.listProjectBackend.controller;

import com.lsb.listProjectBackend.domain.DatasetTO;
import com.lsb.listProjectBackend.domain.ScrapyConfigTO;
import com.lsb.listProjectBackend.service.DatasetService;
import com.lsb.listProjectBackend.service.ScrapyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
public class DatasetController {
    @Autowired
    private DatasetService datasetService;

    @GetMapping("/dataset/get")
    DatasetTO getDataset(@RequestParam("name") String name) {
        return datasetService.getDataset(name);
    }

    @GetMapping("/dataset/exist")
    boolean existDataset(@RequestParam("name") String name) {
        return datasetService.existDataset(name);
    }

    @GetMapping("/dataset/all")
    List<DatasetTO> getAllConfig() {
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
}
