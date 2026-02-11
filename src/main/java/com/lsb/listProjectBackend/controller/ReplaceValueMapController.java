package com.lsb.listProjectBackend.controller;

import com.lsb.listProjectBackend.domain.ReplaceValueMapTO;
import com.lsb.listProjectBackend.service.ReplaceValueMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
public class ReplaceValueMapController {
    @Autowired
    private ReplaceValueMapService replaceValueMapService;

    @GetMapping("/replace-value-map/name-list")
    List<ReplaceValueMapTO> getNameList() {
        return replaceValueMapService.getNameList();
    }

    @GetMapping("/replace-value-map/exist")
    boolean existMap(@RequestParam("name") String name) {
        return replaceValueMapService.existMap(name);
    }

    @GetMapping("/replace-value-map/get")
    ReplaceValueMapTO getByName(@RequestParam("name") String name) {
        return replaceValueMapService.getByName(name);
    }

    @PostMapping("/replace-value-map/update")
    void update(@RequestBody ReplaceValueMapTO to) {
        replaceValueMapService.update(to);
    }

    @DeleteMapping("/replace-value-map/delete")
    void deleteByName(@RequestParam("name") String name) {
        replaceValueMapService.deleteByName(name);
    }
}
