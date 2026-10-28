package com.lsb.listProjectBackend.controller.spider;

import com.lsb.listProjectBackend.domain.spider.SpiderPaginationTO;
import com.lsb.listProjectBackend.service.spider.SpiderPaginationService;
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
public class SpiderPaginationController {
    private final SpiderPaginationService spiderPaginationService;

    @GetMapping("/spider-pagination/all")
    public List<SpiderPaginationTO> getAll() {
        return spiderPaginationService.getAll();
    }

    @GetMapping("/spider-pagination")
    public SpiderPaginationTO getById(@RequestParam("spiderPaginationId") String spiderPaginationId) {
        return spiderPaginationService.getById(spiderPaginationId);
    }

    @PostMapping("/spider-pagination/update")
    public void update(@RequestBody SpiderPaginationTO req) {
        spiderPaginationService.update(req);
    }

    @DeleteMapping("/spider-pagination/delete")
    public void delete(@RequestParam("spiderPaginationId") String spiderPaginationId) {
        spiderPaginationService.delete(spiderPaginationId);
    }

    @GetMapping("/spider-pagination/in-use")
    public boolean inUse(@RequestParam("spiderPaginationId") String spiderPaginationId) {
        return spiderPaginationService.exists(spiderPaginationId);
    }
}
