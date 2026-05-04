package com.lsb.listProjectBackend.controller.spider;

import com.lsb.listProjectBackend.domain.spider.SpiderReqTO;
import com.lsb.listProjectBackend.domain.spider.SpiderTestTO;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderItemSetting;
import com.lsb.listProjectBackend.service.spider.SpiderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class SpiderController {
    private final SpiderService spiderService;

    @PostMapping("/spider/use-url")
    public String executeByUrl(@RequestBody SpiderReqTO req) throws Exception {
        return spiderService.executeByUrl(req.spiderId(), req.url());
    }

    @PostMapping("/spider/use-prime-key")
    public String executeByPrimeKeyList(@RequestBody SpiderReqTO req) throws Exception {
        return spiderService.executeByPrimeKeyList(req.spiderId(), req.primeKeyList());
    }

    @PostMapping("/spider/preview-extraction")
    public String previewExtraction(@RequestBody SpiderItemSetting setting) {
        return spiderService.previewExtraction(setting);
    }

    @PostMapping("/spider/preview/use-url")
    public String previewByUrl(@RequestBody SpiderTestTO req) throws Exception {
        return spiderService.previewByUrl(req);
    }

    @PostMapping("/spider/preview/use-prime-key")
    public String previewByPrimeKey(@RequestBody SpiderTestTO req) throws Exception {
        return spiderService.previewByPrimeKey(req);
    }

}