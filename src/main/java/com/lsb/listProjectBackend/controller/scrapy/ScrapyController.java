package com.lsb.listProjectBackend.controller.scrapy;

import com.lsb.listProjectBackend.domain.scrapy.*;
import com.lsb.listProjectBackend.service.scrapy.ScrapyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class ScrapyController {
    private final ScrapyService scrapyService;

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

    @PostMapping("/scrapy/by-name-list")
    List<ScrapyConfigTO> getByNameList(@RequestBody List<String> nameList) {
        return scrapyService.getByNameList(nameList);
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
        return scrapyService.testParseHtml(req.scrapyData().getHtml(), req.scrapyData());
    }

    @PostMapping("/scrapy/test/json")
    public Map<String, Object> testJson(@RequestBody ScrapyTestTO req) throws Exception {
        return scrapyService.doScrapyByJson(req.json(), req.scrapyDataList());
    }

    @PostMapping("/scrapy/test/url")
    public Map<String, Object> testUrl(@RequestBody ScrapyTestTO req) throws Exception {
        return scrapyService.doScrapyByUrl(req.url(), req.scrapyDataList());
    }

    @PostMapping("/scrapy/use-json")
    public Map<String, Object> scrapyByJson(@RequestBody ScrapyReqTO req) throws Exception {
        return scrapyService.scrapyByJson(req);
    }

    @PostMapping("/scrapy/use-url")
    public Map<String, Object> scrapyByUrl(@RequestBody ScrapyReqTO req) throws Exception {
        return scrapyService.scrapyByUrl(req);
    }
}
