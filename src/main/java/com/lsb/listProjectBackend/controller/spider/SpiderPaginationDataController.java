package com.lsb.listProjectBackend.controller.spider;

import com.lsb.listProjectBackend.domain.spider.SpiderPaginationDataTO;
import com.lsb.listProjectBackend.service.spider.SpiderPaginationDataService;
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
@RequestMapping("api")
@RequiredArgsConstructor
public class SpiderPaginationDataController {
    private final SpiderPaginationDataService spiderPaginationDataService;

    @GetMapping("/spider-pagination-data/all")
    public List<SpiderPaginationDataTO> getAll() {
        return spiderPaginationDataService.getAll();
    }

    @GetMapping("/spider-pagination-data/by-spider-pagination-id")
    public List<SpiderPaginationDataTO> getBySpiderPaginationId(
            @RequestParam("spiderPaginationId") String spiderPaginationId) {
        return spiderPaginationDataService.getBySpiderPaginationId(spiderPaginationId);
    }

    @GetMapping("/spider-pagination-data")
    public SpiderPaginationDataTO getById(@RequestParam("spiderPaginationId") String spiderPaginationId,
            @RequestParam("dataId") String dataId) {
        return spiderPaginationDataService.getById(spiderPaginationId, dataId);
    }

    @PostMapping("/spider-pagination-data/update")
    public void update(@RequestBody SpiderPaginationDataTO req) {
        spiderPaginationDataService.update(req);
    }

    @DeleteMapping("/spider-pagination-data/delete")
    public void delete(@RequestParam("spiderPaginationId") String spiderPaginationId,
            @RequestParam("dataId") String dataId) {
        spiderPaginationDataService.delete(spiderPaginationId, dataId);
    }

    @DeleteMapping("/spider-pagination-data/delete-by-spider-pagination-id")
    public void deleteBySpiderPaginationId(@RequestParam("spiderPaginationId") String spiderPaginationId) {
        spiderPaginationDataService.deleteBySpiderPaginationId(spiderPaginationId);
    }

    @GetMapping("/spider-pagination-data/in-use")
    public boolean inUse(@RequestParam("spiderPaginationId") String spiderPaginationId,
            @RequestParam("dataId") String dataId) {
        return spiderPaginationDataService.exists(spiderPaginationId, dataId);
    }
}
