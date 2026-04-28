package com.lsb.listProjectBackend.controller.spider;

import com.lsb.listProjectBackend.domain.spider.SpiderMappingTO;
import com.lsb.listProjectBackend.service.spider.SpiderMappingService;
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
public class SpiderMappingController {

    @Autowired
    private SpiderMappingService spiderMappingService;

    @GetMapping("/spider-mapping/all")
    public List<SpiderMappingTO> getAllMapping() {
        return spiderMappingService.getAll();
    }

    @GetMapping("/spider-mapping/by-spider-id")
    public List<SpiderMappingTO> getMappingBySpiderId(@RequestParam("spiderId") String spiderId) {
        return spiderMappingService.getBySpiderId(spiderId);
    }

    @GetMapping("/spider-mapping")
    public SpiderMappingTO getMapping(@RequestParam("spiderId") String spiderId,
            @RequestParam("executionOrder") Integer executionOrder) {
        return spiderMappingService.getById(spiderId, executionOrder);
    }

    @PostMapping("/spider-mapping/update")
    public void updateMapping(@RequestBody SpiderMappingTO req) {
        spiderMappingService.update(req);
    }

    @PostMapping("/spider-mapping/update-list")
    public void updateMappingList(@RequestBody List<SpiderMappingTO> req) {
        spiderMappingService.updateList(req);
    }

    @DeleteMapping("/spider-mapping/delete")
    public void deleteMapping(@RequestParam("spiderId") String spiderId,
            @RequestParam("executionOrder") Integer executionOrder, @RequestParam("spiderItemId") String spiderItemId) {
        spiderMappingService.delete(spiderId, executionOrder, spiderItemId);
    }

    @DeleteMapping("/spider-mapping/delete-by-spider-id")
    public void deleteMappingBySpiderId(@RequestParam("spiderId") String spiderId) {
        spiderMappingService.deleteBySpiderId(spiderId);
    }

    @GetMapping("/spider-mapping/in-use-by-spider-item-id")
    public boolean inUseBySpiderItemId(@RequestParam("spiderItemId") String spiderItemId) {
        return spiderMappingService.inUseBySpiderItemId(spiderItemId);
    }
}