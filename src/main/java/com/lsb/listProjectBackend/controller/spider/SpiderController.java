package com.lsb.listProjectBackend.controller.spider;

import com.lsb.listProjectBackend.domain.spider.SpiderItemTO;
import com.lsb.listProjectBackend.domain.spider.SpiderPaginationDataTO;
import java.util.List;
import com.lsb.listProjectBackend.domain.spider.SpiderReqTO;
import com.lsb.listProjectBackend.service.spider.SpiderService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class SpiderController {
    private final SpiderService spiderService;

    @PostMapping("/spider/execute-pagination")
    public List<SpiderPaginationDataTO> executePagination(
            @RequestParam("spiderPaginationId") String spiderPaginationId,
            @RequestParam(value = "force", defaultValue = "false") boolean force) throws Exception {
        return spiderService.executePagination(spiderPaginationId, force);
    }

    @PostMapping("/spider/use-url")
    public String executeByUrl(@RequestBody SpiderReqTO req) throws Exception {
        return spiderService.executeByUrl(req.spiderId(), req.url());
    }

    @PostMapping("/spider/use-prime-key")
    public String executeByPrimeKeyList(@RequestBody SpiderReqTO req) throws Exception {
        return spiderService.executeByPrimeKeyList(req.spiderId(), req.primeKeyList());
    }

    @PostMapping("/spider/preview-extraction")
    public String previewExtraction(@RequestBody SpiderItemTO req) {
        return spiderService.previewExtraction(req);
    }

    @PostMapping(value = { "/spider/preview-extraction-with-file" }, consumes = {
            "multipart/form-data" })
    public String previewExtractionWithFile(
            @RequestPart("file") MultipartFile file,
            @RequestPart("setting") SpiderItemTO setting) throws Exception {
        return spiderService.previewExtractionWithFile(file, setting);
    }
}
