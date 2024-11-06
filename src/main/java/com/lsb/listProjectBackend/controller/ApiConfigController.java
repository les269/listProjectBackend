package com.lsb.listProjectBackend.controller;

import com.lsb.listProjectBackend.domain.ApiConfigTO;
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

    @PostMapping("/api-config/all/name")
    public List<ApiConfigTO> getListByName(@RequestBody List<String> nameList){
        return this.apiConfigService.getListById(nameList);
    }

    @PostMapping("/api-config/name")
    public ApiConfigTO getByName(@RequestParam("name") String name){
        return this.apiConfigService.getByName(name);
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
