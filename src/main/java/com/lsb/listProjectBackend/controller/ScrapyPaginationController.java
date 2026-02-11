package com.lsb.listProjectBackend.controller;

import com.lsb.listProjectBackend.domain.ScrapyPaginationTO;
import com.lsb.listProjectBackend.domain.ScrapyPaginationTestTO;
import com.lsb.listProjectBackend.service.ScrapyPaginationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
public class ScrapyPaginationController {
    @Autowired
    private ScrapyPaginationService scrapyPaginationService;

    @GetMapping("/scrapy-pagination/get")
    ScrapyPaginationTO get(@RequestParam("name") String name) {
        return scrapyPaginationService.get(name);
    }

    @GetMapping("/scrapy-pagination/exist")
    boolean exist(@RequestParam("name") String name) {
        return scrapyPaginationService.exist(name);
    }

    @GetMapping("/scrapy-pagination/all")
    List<ScrapyPaginationTO> getAll() {
        return scrapyPaginationService.getAll();
    }

    @PostMapping("/scrapy-pagination/update")
    void update(@RequestBody ScrapyPaginationTO req) {
        scrapyPaginationService.update(req);
    }

    @DeleteMapping("/scrapy-pagination/delete")
    void delete(@RequestParam("name") String name) {
        scrapyPaginationService.delete(name);
    }

    @PostMapping("/scrapy-pagination/test/html")
    public Map<String, Object> testHtml(@RequestBody ScrapyPaginationTestTO req) throws Exception {
        return scrapyPaginationService.testHtml(req);
    }

    @GetMapping("/scrapy-pagination/update-redirect-data")
    public ScrapyPaginationTO updateRedirectData(@RequestParam("name") String name) throws Exception {
        return scrapyPaginationService.updateRedirectData(name);
    }
}
