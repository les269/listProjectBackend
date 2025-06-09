package com.lsb.listProjectBackend.controller;

import com.lsb.listProjectBackend.domain.SettingTO;
import com.lsb.listProjectBackend.service.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
public class SettingController {

    @Autowired
    private SettingService settingService;

    @GetMapping("/setting/all")
    List<SettingTO> getAll() {
        return settingService.getAll();
    }

    @PostMapping("/setting/update")
    void update(@RequestBody SettingTO req) {
        settingService.update(req);
    }

    @PostMapping("/setting/update/all")
    void update(@RequestBody List<SettingTO> req) {
        settingService.updateAll(req);
    }
}
