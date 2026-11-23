package com.lsb.listProjectBackend.controller.connection;

import com.lsb.listProjectBackend.domain.connection.ApiRequestConfigTO;
import com.lsb.listProjectBackend.domain.connection.SingalCallApiReqTO;
import com.lsb.listProjectBackend.service.connection.ApiRequestConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import tools.jackson.databind.JsonNode;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/api-config")
@RequiredArgsConstructor
public class ApiRequestConfigController {
    private final ApiRequestConfigService apiRequestConfigService;

    @GetMapping("/exist")
    public boolean isExist(@RequestParam("apiId") String apiId){
        return this.apiRequestConfigService.isExist(apiId);
    }

    @GetMapping("/all")
    public List<ApiRequestConfigTO> getAll(){
        return this.apiRequestConfigService.getAll();
    }

    @PostMapping("/all/id")
    public List<ApiRequestConfigTO> getListById(@RequestBody List<String> apiIds){
        return this.apiRequestConfigService.getListById(apiIds);
    }

    @GetMapping("/id")
    public ApiRequestConfigTO getById(@RequestParam("apiId") String apiId){
        return this.apiRequestConfigService.getById(apiId);
    }

    @PostMapping("/update")
    public void update(@RequestBody ApiRequestConfigTO apiRequestConfigTO){
        this.apiRequestConfigService.update(apiRequestConfigTO);
    }

    @PostMapping("/singalCallApi")
    public JsonNode singalCallApi(@RequestBody SingalCallApiReqTO request) throws Exception {
        return this.apiRequestConfigService.singalCallApi(request);
    }

    @PostMapping("/delete")
    public void delete(@RequestBody ApiRequestConfigTO apiRequestConfigTO){
        this.apiRequestConfigService.delete(apiRequestConfigTO);
    }
}
