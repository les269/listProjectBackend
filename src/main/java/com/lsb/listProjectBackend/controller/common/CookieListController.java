package com.lsb.listProjectBackend.controller.common;

import com.lsb.listProjectBackend.domain.common.CookieListTO;
import com.lsb.listProjectBackend.service.common.CookieListService;
import com.lsb.listProjectBackend.utils.Global;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
public class CookieListController {

    @Autowired
    private CookieListService cookieListService;

    @GetMapping("/cookie-list/by-ref-id-and-type")
    public CookieListTO getByRefIdAndType(@RequestParam("refId") String refId,
            @RequestParam("type") Global.CookieListMapType type) {
        return cookieListService.getByRefIdAndType(refId, type);
    }

    @GetMapping("/cookie-list/all")
    public List<CookieListTO> getAll() {
        return cookieListService.getAll();
    }

    @GetMapping("/cookie-list")
    public CookieListTO getByCookieId(@RequestParam("cookieId") String cookieId) {
        return cookieListService.getByCookieId(cookieId);
    }

    @PostMapping("/cookie-list/update")
    public void update(@RequestBody CookieListTO req) {
        cookieListService.update(req);
    }

    @DeleteMapping("/cookie-list/delete")
    public void delete(@RequestParam("cookieId") String cookieId) {
        cookieListService.delete(cookieId);
    }

}
