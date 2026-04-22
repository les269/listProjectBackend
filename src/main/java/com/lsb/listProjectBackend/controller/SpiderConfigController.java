package com.lsb.listProjectBackend.controller;

import com.lsb.listProjectBackend.domain.SpiderConfigTO;
import com.lsb.listProjectBackend.service.SpiderConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
public class SpiderConfigController {

    @Autowired
    private SpiderConfigService spiderConfigService;

    @GetMapping("/spider-config/all")
    public List<SpiderConfigTO> getAllConfig() {
        return spiderConfigService.getAll();
    }

    @GetMapping("/spider-config")
    public SpiderConfigTO getConfig(@RequestParam("spiderId") String spiderId) {
        return spiderConfigService.getById(spiderId);
    }

    @PostMapping("/spider-config/update")
    public void updateConfig(@RequestBody SpiderConfigTO req) {
        spiderConfigService.update(req);
    }

    @DeleteMapping("/spider-config/delete")
    public void deleteConfig(@RequestParam("spiderId") String spiderId) {
        spiderConfigService.delete(spiderId);
    }

    @GetMapping("/spider-config/in-use")
    public boolean isConfigInUse(@RequestParam("spiderId") String spiderId) {
        return spiderConfigService.exists(spiderId);
    }
}