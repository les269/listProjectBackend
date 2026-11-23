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
@RequestMapping("/api/spider-pagination")
@RequiredArgsConstructor
public class SpiderPaginationController {
    private final SpiderPaginationService spiderPaginationService;

    @GetMapping("/all")
    public List<SpiderPaginationTO> getAll() {
        return spiderPaginationService.getAll();
    }

    @GetMapping
    public SpiderPaginationTO getById(@RequestParam("spiderPaginationId") String spiderPaginationId) {
        return spiderPaginationService.getById(spiderPaginationId);
    }

    @PostMapping("/update")
    public void update(@RequestBody SpiderPaginationTO req) {
        spiderPaginationService.update(req);
    }

    @DeleteMapping("/delete")
    public void delete(@RequestParam("spiderPaginationId") String spiderPaginationId) {
        spiderPaginationService.delete(spiderPaginationId);
    }

    @GetMapping("/in-use")
    public boolean inUse(@RequestParam("spiderPaginationId") String spiderPaginationId) {
        return spiderPaginationService.exists(spiderPaginationId);
    }
}
