package com.lsb.listProjectBackend.controller.file;

import com.lsb.listProjectBackend.domain.file.DiskTO;
import com.lsb.listProjectBackend.service.file.DiskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class DiskController {
    private final DiskService diskService;

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
