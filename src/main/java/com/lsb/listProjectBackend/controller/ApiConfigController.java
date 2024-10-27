package com.lsb.listProjectBackend.controller;

import com.lsb.listProjectBackend.domain.ApiConfigTO;
import com.lsb.listProjectBackend.entity.ApiConfigPK;
import com.lsb.listProjectBackend.service.ApiConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
public class ApiConfigController {
    @Autowired
    private ApiConfigService apiConfigService;

    @GetMapping("/api-config/all")
    public List<ApiConfigTO> getAll(){
        return this.apiConfigService.getAll();
    }

    @PostMapping("/api-config/all/id")
    public List<ApiConfigTO> getListById(@RequestBody List<ApiConfigPK> pkList){
        return this.apiConfigService.getListById(pkList);
    }

    @PostMapping("/api-config/update")
    public void update(@RequestBody ApiConfigTO apiConfigTO){
        this.apiConfigService.update(apiConfigTO);
    }

    @PostMapping("/api-config/delete")
    public void delete(@RequestBody ApiConfigTO apiConfigTO){
        this.apiConfigService.delete(apiConfigTO);
    }
}
