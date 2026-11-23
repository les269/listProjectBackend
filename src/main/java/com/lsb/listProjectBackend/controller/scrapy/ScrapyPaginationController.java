package com.lsb.listProjectBackend.controller.scrapy;

import com.lsb.listProjectBackend.domain.scrapy.ScrapyPaginationTO;
import com.lsb.listProjectBackend.domain.scrapy.ScrapyPaginationTestTO;
import com.lsb.listProjectBackend.service.scrapy.ScrapyPaginationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/scrapy-pagination")
@RequiredArgsConstructor
public class ScrapyPaginationController {
    private final ScrapyPaginationService scrapyPaginationService;

    @GetMapping("/get")
    ScrapyPaginationTO get(@RequestParam("name") String name) {
        return scrapyPaginationService.get(name);
    }

    @GetMapping("/exist")
    boolean exist(@RequestParam("name") String name) {
        return scrapyPaginationService.exist(name);
    }

    @GetMapping("/all")
    List<ScrapyPaginationTO> getAll() {
        return scrapyPaginationService.getAll();
    }

    @PostMapping("/update")
    void update(@RequestBody ScrapyPaginationTO req) {
        scrapyPaginationService.update(req);
    }

    @DeleteMapping("/delete")
    void delete(@RequestParam("name") String name) {
        scrapyPaginationService.delete(name);
    }

    @PostMapping("/test/html")
    public Map<String, Object> testHtml(@RequestBody ScrapyPaginationTestTO req) throws Exception {
        return scrapyPaginationService.testHtml(req);
    }

    @GetMapping("/update-redirect-data")
    public ScrapyPaginationTO updateRedirectData(@RequestParam("name") String name) throws Exception {
        return scrapyPaginationService.updateRedirectData(name);
    }
}
