package com.lsb.listProjectBackend.controller.theme;

import com.lsb.listProjectBackend.domain.theme.ThemeHiddenTO;
import com.lsb.listProjectBackend.service.theme.ThemeHiddenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/theme-hidden")
@RequiredArgsConstructor
public class ThemeHiddenController {
    private final ThemeHiddenService themeHiddenService;

    @GetMapping("/all")
    List<ThemeHiddenTO> getAll() {
        return themeHiddenService.getAll();
    }

    @PostMapping("/save")
    void save(@RequestBody ThemeHiddenTO req) {
        themeHiddenService.save(req);
    }

    @DeleteMapping("/delete")
    void delete(@RequestParam("headerId") String headerId) {
        themeHiddenService.delete(headerId);
    }
}
