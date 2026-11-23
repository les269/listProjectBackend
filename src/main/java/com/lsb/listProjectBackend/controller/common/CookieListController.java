package com.lsb.listProjectBackend.controller.common;

import com.lsb.listProjectBackend.domain.common.CookieListTO;
import com.lsb.listProjectBackend.service.common.CookieListService;
import com.lsb.listProjectBackend.utils.Global;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/cookie-list")
@RequiredArgsConstructor
public class CookieListController {
    private final CookieListService cookieListService;

    @GetMapping("/by-ref-id-and-type")
    public CookieListTO getByRefIdAndType(@RequestParam("refId") String refId,
            @RequestParam("type") Global.CookieListMapType type) {
        return cookieListService.getByRefIdAndType(refId, type);
    }

    @GetMapping("/all")
    public List<CookieListTO> getAll() {
        return cookieListService.getAll();
    }

    @GetMapping
    public CookieListTO getByCookieId(@RequestParam("cookieId") String cookieId) {
        return cookieListService.getByCookieId(cookieId);
    }

    @PostMapping("/update")
    public void update(@RequestBody CookieListTO req) {
        cookieListService.update(req);
    }

    @DeleteMapping("/delete")
    public void delete(@RequestParam("cookieId") String cookieId) {
        cookieListService.delete(cookieId);
    }

}
