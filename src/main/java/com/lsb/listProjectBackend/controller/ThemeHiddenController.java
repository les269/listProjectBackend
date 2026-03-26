package com.lsb.listProjectBackend.controller;

import com.lsb.listProjectBackend.domain.ThemeHiddenTO;
import com.lsb.listProjectBackend.service.ThemeHiddenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
public class ThemeHiddenController {

    @Autowired
    private ThemeHiddenService themeHiddenService;

    @GetMapping("/theme-hidden/all")
    List<ThemeHiddenTO> getAll() {
        return themeHiddenService.getAll();
    }

    @PostMapping("/theme-hidden/save")
    void save(@RequestBody ThemeHiddenTO req) {
        themeHiddenService.save(req);
    }

    @DeleteMapping("/theme-hidden/delete")
    void delete(@RequestParam("headerId") String headerId) {
        themeHiddenService.delete(headerId);
    }
}
