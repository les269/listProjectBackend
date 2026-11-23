package com.lsb.listProjectBackend.controller.common;

import com.lsb.listProjectBackend.domain.common.SettingTO;
import com.lsb.listProjectBackend.service.common.SettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/setting")
@RequiredArgsConstructor
public class SettingController {
    private final SettingService settingService;

    @GetMapping("/all")
    List<SettingTO> getAll() {
        return settingService.getAll();
    }

    @PostMapping("/update")
    void update(@RequestBody SettingTO req) {
        settingService.update(req);
    }

    @PostMapping("/update/all")
    void update(@RequestBody List<SettingTO> req) {
        settingService.updateAll(req);
    }

    @GetMapping("/get-by-name")
    SettingTO getByName(@RequestParam("name") String name){
        return settingService.getByName(name);
    }

    @PostMapping("/changeDatabase")
    void changeDatabase(@RequestBody SettingTO req) {
        settingService.changeDatabase(req);
    }
}
