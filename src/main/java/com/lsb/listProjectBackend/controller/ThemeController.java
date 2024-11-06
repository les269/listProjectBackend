package com.lsb.listProjectBackend.controller;

import com.lsb.listProjectBackend.domain.*;
import com.lsb.listProjectBackend.service.ThemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
public class ThemeController {
    @Autowired
    private ThemeService themeService;

    @GetMapping("/theme/all")
    List<ThemeHeaderTO> getAllTheme() {
        return themeService.getAllTheme();
    }

    @GetMapping("/theme/id")
    ThemeHeaderTO getByHeaderId(@RequestParam("headerId") String headerId) {
        return themeService.getByHeaderId(headerId);
    }

    @PostMapping("/theme/one")
    ThemeHeaderTO findTheme(@RequestBody ThemeHeaderTO headerTO) {
        return themeService.findTheme(headerTO);
    }

    @PostMapping("/theme/exist")
    boolean existTheme(@RequestBody ThemeHeaderTO headerTO) {
        return themeService.existTheme(headerTO);
    }

    @PostMapping("/theme/update")
    void updateTheme(@RequestBody ThemeHeaderTO theme) throws Exception {
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

    @PostMapping("/theme/custom/value")
    Map<String, Map<String, String>> findCustomValue(@RequestBody ThemeCustomValueRequest request) throws Exception {
        return themeService.findCustomValue(request);
    }

    @PostMapping("/theme/custom/update")
    void updateCustomValue(@RequestBody ThemeCustomValueTO customValueTO){
        themeService.updateCustomValue(customValueTO);
    }

    @PostMapping("/theme/tag/update")
    void updateTagValue(@RequestBody List<ThemeTagValueTO> list) {
        themeService.updateTagValue(list);
    }

    @GetMapping("/theme/tag/value")
    List<ThemeTagValueTO> getTagValueList(@RequestParam("headerId") String headerId) {
        return themeService.getTagValueList(headerId);
    }
}
