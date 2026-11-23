package com.lsb.listProjectBackend.controller.scrapy;

import com.lsb.listProjectBackend.domain.scrapy.*;
import com.lsb.listProjectBackend.service.scrapy.ScrapyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/scrapy")
@RequiredArgsConstructor
public class ScrapyController {
    private final ScrapyService scrapyService;

    @GetMapping("/get")
    ScrapyConfigTO getConfig(@RequestParam("name") String name) {
        return scrapyService.getConfig(name);
    }

    @GetMapping("/exist")
    boolean existConfig(@RequestParam("name") String name) {
        return scrapyService.existConfig(name);
    }

    @GetMapping("/all")
    List<ScrapyConfigTO> getAllConfig() {
        return scrapyService.getAllConfig();
    }

    @GetMapping("/allName")
    List<String> getAllName() {
        return scrapyService.getAllName();
    }

    @PostMapping("/by-name-list")
    List<ScrapyConfigTO> getByNameList(@RequestBody List<String> nameList) {
        return scrapyService.getByNameList(nameList);
    }

    @PostMapping("/update")
    void updateConfig(@RequestBody ScrapyConfigTO req) {
        scrapyService.updateConfig(req);
    }

    @DeleteMapping("/delete")
    void deleteConfig(@RequestParam("name") String name) {
        scrapyService.deleteConfig(name);
    }

    @PostMapping("/test/html")
    public Map<String, Object> testHtml(@RequestBody ScrapyTestTO req) {
        return scrapyService.testParseHtml(req.scrapyData().getHtml(), req.scrapyData());
    }

    @PostMapping("/test/json")
    public Map<String, Object> testJson(@RequestBody ScrapyTestTO req) throws Exception {
        return scrapyService.doScrapyByJson(req.json(), req.scrapyDataList());
    }

    @PostMapping("/test/url")
    public Map<String, Object> testUrl(@RequestBody ScrapyTestTO req) throws Exception {
        return scrapyService.doScrapyByUrl(req.url(), req.scrapyDataList());
    }

    @PostMapping("/use-json")
    public Map<String, Object> scrapyByJson(@RequestBody ScrapyReqTO req) throws Exception {
        return scrapyService.scrapyByJson(req);
    }

    @PostMapping("/use-url")
    public Map<String, Object> scrapyByUrl(@RequestBody ScrapyReqTO req) throws Exception {
        return scrapyService.scrapyByUrl(req);
    }
}
