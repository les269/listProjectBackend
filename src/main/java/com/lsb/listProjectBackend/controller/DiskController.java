package com.lsb.listProjectBackend.controller;

import com.lsb.listProjectBackend.domain.DiskTO;
import com.lsb.listProjectBackend.service.DiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
public class DiskController {

    @Autowired
    private DiskService diskService;

    @GetMapping("/disk/all")
    public List<DiskTO> getAll() {
        return diskService.getAll();
    }

    @PostMapping("/disk/add")
    public void add(@RequestBody String disk) {
        diskService.add(disk);
    }

    @PostMapping("/disk/delete")
    public void delete(@RequestBody String disk) {
        diskService.delete(disk);
    }

    @GetMapping("/disk/refresh")
    public void refresh() {
        diskService.refresh();
    }
}
