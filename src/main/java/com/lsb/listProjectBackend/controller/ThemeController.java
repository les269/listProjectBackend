package com.lsb.listProjectBackend.controller;

import com.lsb.listProjectBackend.domain.CopyThemeRequest;
import com.lsb.listProjectBackend.domain.ThemeHeaderTO;
import com.lsb.listProjectBackend.domain.ThemeRequest;
import com.lsb.listProjectBackend.domain.ThemeResponse;
import com.lsb.listProjectBackend.service.ThemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
public class ThemeController {
    @Autowired
    private ThemeService themeService;

    @GetMapping("/theme/all")
    List<ThemeHeaderTO> getAllTheme() {
        return themeService.getAllTheme();
    }

    @PostMapping("/theme/one")
    ThemeResponse findTheme(@RequestBody ThemeHeaderTO headerTO) {
        return themeService.findTheme(headerTO);
    }

    @PostMapping("/theme/exist")
    boolean existTheme(@RequestBody ThemeHeaderTO headerTO) {
        return themeService.existTheme(headerTO);
    }

    @PostMapping("/theme/update")
    void updateTheme(@RequestBody ThemeRequest theme) throws Exception {
        themeService.updateTheme(theme);
    }

    @PostMapping("/theme/delete")
    void deleteTheme(@RequestBody ThemeHeaderTO headerTO) throws Exception {
        themeService.deleteTheme(headerTO);
    }
    @PostMapping("/theme/copy")
    void copyTheme(@RequestBody CopyThemeRequest copyThemeRequest) throws Exception {
        themeService.copyTheme(copyThemeRequest);
    }
}
