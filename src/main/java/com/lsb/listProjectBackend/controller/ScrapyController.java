package com.lsb.listProjectBackend.controller;

import com.lsb.listProjectBackend.domain.*;
import com.lsb.listProjectBackend.service.ScrapyService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
public class ScrapyController {
    @Autowired
    private ScrapyService scrapyService;

    @GetMapping("/scrapy/get")
    ScrapyConfigTO getConfig(@RequestParam("name") String name) {
        return scrapyService.getConfig(name);
    }

    @GetMapping("/scrapy/exist")
    boolean existConfig(@RequestParam("name") String name) {
        return scrapyService.existConfig(name);
    }

    @GetMapping("/scrapy/all")
    List<ScrapyConfigTO> getAllConfig() {
        return scrapyService.getAllConfig();
    }

    @GetMapping("/scrapy/allName")
    List<String> getAllName() {
        return scrapyService.getAllName();
    }

    @PostMapping("/scrapy/update")
    void updateConfig(@RequestBody ScrapyConfigTO req) {
        scrapyService.updateConfig(req);
    }

    @DeleteMapping("/scrapy/delete")
    void deleteConfig(@RequestParam("name") String name) {
        scrapyService.deleteConfig(name);
    }

    @PostMapping("/scrapy/test/html")
    public Map<String, Object> testHtml(@RequestBody ScrapyTestTO req) {
        return scrapyService.testParseHtml(req.getScrapyData().getHtml(), req.getScrapyData());
    }

    @PostMapping("/scrapy/test/json")
    public Map<String, Object> testJson(@RequestBody ScrapyTestTO req) throws Exception {
        return scrapyService.scrapyByJson(req.getScrapyDataList(), req.getJson());
    }

    @PostMapping("/scrapy/test/url")
    public Map<String, Object> testUrl(@RequestBody ScrapyTestTO req) throws Exception {
        return scrapyService.testUrl(req);
    }
}
