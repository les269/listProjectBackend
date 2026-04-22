package com.lsb.listProjectBackend.controller;

import com.lsb.listProjectBackend.domain.CookieListMapTO;
import com.lsb.listProjectBackend.service.CookieListMapService;
import com.lsb.listProjectBackend.utils.Global;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
public class CookieListMapController {

    @Autowired
    private CookieListMapService cookieListMapService;

    @GetMapping("/cookie-list/map/by-id-and-type")
    public CookieListMapTO getMapByIdAndType(@RequestParam("refId") String refId,
            @RequestParam("type") Global.CookieListMapType type) {
        return cookieListMapService.getMapByIdAndType(refId, type);
    }

    @GetMapping("/cookie-list/map/by-ref-id")
    public List<CookieListMapTO> getMapByRefId(@RequestParam("refId") String refId) {
        return cookieListMapService.getMapByRefId(refId);
    }

    @GetMapping("/cookie-list/map/by-type")
    public List<CookieListMapTO> getMapByType(@RequestParam("type") Global.CookieListMapType type) {
        return cookieListMapService.getMapByType(type);
    }

    @PostMapping("/cookie-list/map/update")
    public void updateMap(@RequestBody CookieListMapTO req) {
        cookieListMapService.updateMap(req);
    }

    @DeleteMapping("/cookie-list/map/delete")
    public void deleteMap(@RequestParam("refId") String refId,
            @RequestParam("type") Global.CookieListMapType type) {
        cookieListMapService.deleteMap(refId, type);
    }

    @GetMapping("/cookie-list/map/in-use")
    public boolean isInUseMap(@RequestParam("cookieId") String cookieId,
            @RequestParam("type") Global.CookieListMapType type) {
        return cookieListMapService.isInUseMap(cookieId, type);
    }

    @GetMapping("/cookie-list/map/cookie-in-use")
    public boolean cookieIsInUse(@RequestParam("cookieId") String cookieId) {
        return cookieListMapService.cookieIsInUse(cookieId);
    }
}
