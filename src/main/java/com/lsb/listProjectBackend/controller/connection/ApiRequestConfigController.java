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
@RequestMapping("api")
@RequiredArgsConstructor
public class ApiRequestConfigController {
    private final ApiRequestConfigService apiRequestConfigService;

    @GetMapping("/api-config/exist")
    public boolean isExist(@RequestParam("apiId") String apiId){
        return this.apiRequestConfigService.isExist(apiId);
    }

    @GetMapping("/api-config/all")
    public List<ApiRequestConfigTO> getAll(){
        return this.apiRequestConfigService.getAll();
    }

    @PostMapping("/api-config/all/id")
    public List<ApiRequestConfigTO> getListById(@RequestBody List<String> apiIds){
        return this.apiRequestConfigService.getListById(apiIds);
    }

    @GetMapping("/api-config/id")
    public ApiRequestConfigTO getById(@RequestParam("apiId") String apiId){
        return this.apiRequestConfigService.getById(apiId);
    }

    @PostMapping("/api-config/update")
    public void update(@RequestBody ApiRequestConfigTO apiRequestConfigTO){
        this.apiRequestConfigService.update(apiRequestConfigTO);
    }

    @PostMapping("/api-config/singalCallApi")
    public JsonNode singalCallApi(@RequestBody SingalCallApiReqTO request) throws Exception {
        return this.apiRequestConfigService.singalCallApi(request);
    }

    @PostMapping("/api-config/delete")
    public void delete(@RequestBody ApiRequestConfigTO apiRequestConfigTO){
        this.apiRequestConfigService.delete(apiRequestConfigTO);
    }
}
