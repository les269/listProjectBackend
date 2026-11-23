package com.lsb.listProjectBackend.controller.common;

import com.lsb.listProjectBackend.domain.common.ReplaceValueMapTO;
import com.lsb.listProjectBackend.service.common.ReplaceValueMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/replace-value-map")
@RequiredArgsConstructor
public class ReplaceValueMapController {
    private final ReplaceValueMapService replaceValueMapService;

    @GetMapping("/name-list")
    List<ReplaceValueMapTO> getNameList() {
        return replaceValueMapService.getNameList();
    }

    @GetMapping("/exist")
    boolean existMap(@RequestParam("name") String name) {
        return replaceValueMapService.existMap(name);
    }

    @GetMapping("/get")
    ReplaceValueMapTO getByName(@RequestParam("name") String name) {
        return replaceValueMapService.getByName(name);
    }

    @PostMapping("/update")
    void update(@RequestBody ReplaceValueMapTO to) {
        replaceValueMapService.update(to);
    }

    @DeleteMapping("/delete")
    void deleteByName(@RequestParam("name") String name) {
        replaceValueMapService.deleteByName(name);
    }
}
