package com.lsb.listProjectBackend.controller.file;

import com.lsb.listProjectBackend.domain.file.DiskTO;
import com.lsb.listProjectBackend.service.file.DiskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/disk")
@RequiredArgsConstructor
public class DiskController {
    private final DiskService diskService;

    @GetMapping("/all")
    public List<DiskTO> getAll() {
        return diskService.getAll();
    }

    @PostMapping("/add")
    public void add(@RequestBody String disk) {
        diskService.add(disk);
    }

    @PostMapping("/delete")
    public void delete(@RequestBody String disk) {
        diskService.delete(disk);
    }

    @GetMapping("/refresh")
    public void refresh() {
        diskService.refresh();
    }
}
