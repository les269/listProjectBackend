package com.lsb.listProjectBackend.controller;

import com.lsb.listProjectBackend.domain.SpiderItemExecuteTO;
import com.lsb.listProjectBackend.domain.SpiderReqTO;
import com.lsb.listProjectBackend.domain.SpiderTestTO;
import com.lsb.listProjectBackend.service.SpiderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
public class SpiderController {

    @Autowired
    private SpiderService spiderService;

    @PostMapping("/spider/use-url")
    public Map<String, Object> executeByUrl(@RequestBody SpiderReqTO req) throws Exception {
        return spiderService.executeByUrl(req.getSpiderId(), req.getUrl());
    }

    @PostMapping("/spider/use-prime-key")
    public Map<String, Object> executeByPrimeKeyList(@RequestBody SpiderReqTO req) throws Exception {
        return spiderService.executeByPrimeKeyList(req.getSpiderId(), req.getPrimeKeyList());
    }
}