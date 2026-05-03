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
@RequestMapping("api")
@RequiredArgsConstructor
public class ScrapyPaginationController {
    private final ScrapyPaginationService scrapyPaginationService;

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
