package com.lsb.listProjectBackend.controller.spider;

import com.lsb.listProjectBackend.domain.spider.SpiderConfigTO;
import com.lsb.listProjectBackend.service.spider.SpiderConfigService;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/spider-config")
@RequiredArgsConstructor
public class SpiderConfigController {
    private final SpiderConfigService spiderConfigService;

    @GetMapping("/all")
    public List<SpiderConfigTO> getAllConfig() {
        return spiderConfigService.getAll();
    }

    @GetMapping
    public SpiderConfigTO getConfig(@RequestParam("spiderId") String spiderId) {
        return spiderConfigService.getById(spiderId);
    }

    @PostMapping("/update")
    public void updateConfig(@RequestBody SpiderConfigTO req) {
        spiderConfigService.update(req);
    }

    @DeleteMapping("/delete")
    public void deleteConfig(@RequestParam("spiderId") String spiderId) {
        spiderConfigService.delete(spiderId);
    }

    @GetMapping("/in-use")
    public boolean isConfigInUse(@RequestParam("spiderId") String spiderId) {
        return spiderConfigService.exists(spiderId);
    }
}