package com.lsb.listProjectBackend.controller.theme;

import com.lsb.listProjectBackend.domain.theme.ThemeHiddenTO;
import com.lsb.listProjectBackend.service.theme.ThemeHiddenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class ThemeHiddenController {
    private final ThemeHiddenService themeHiddenService;

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
