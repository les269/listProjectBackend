package com.lsb.listProjectBackend.controller.theme;

import com.lsb.listProjectBackend.domain.theme.*;
import com.lsb.listProjectBackend.service.theme.ThemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/theme")
@RequiredArgsConstructor
public class ThemeController {
    private final ThemeService themeService;

    @GetMapping("/all")
    List<ThemeHeaderTO> getAllTheme() {
        return themeService.getAllTheme();
    }

    @GetMapping("/id")
    ThemeHeaderTO getByHeaderId(@RequestParam("headerId") String headerId) {
        return themeService.getByHeaderId(headerId);
    }

    @PostMapping("/one")
    ThemeHeaderTO findTheme(@RequestBody ThemeHeaderTO headerTO) {
        return themeService.findTheme(headerTO);
    }

    @PostMapping("/exist")
    boolean existTheme(@RequestBody ThemeHeaderTO headerTO) {
        return themeService.existTheme(headerTO);
    }

    @PostMapping("/update")
    void updateTheme(@RequestBody ThemeHeaderTO theme) throws Exception {
        themeService.updateTheme(theme);
    }

    @PostMapping("/delete")
    void deleteTheme(@RequestBody ThemeHeaderTO headerTO) throws Exception {
        themeService.deleteTheme(headerTO);
    }

    @PostMapping("/copy")
    void copyTheme(@RequestBody CopyThemeReqTO copyThemeRequest) throws Exception {
        themeService.copyTheme(copyThemeRequest);
    }

    @PostMapping("/custom/value")
    Map<String, Map<String, String>> findCustomValue(@RequestBody ThemeCustomValueReqTO request) throws Exception {
        return themeService.findCustomValue(request);
    }

    @PostMapping("/custom/update")
    void updateCustomValue(@RequestBody ThemeCustomValueTO customValueTO) {
        themeService.updateCustomValue(customValueTO);
    }

    @PostMapping("/tag/update")
    void updateTagValue(@RequestBody List<ThemeTagValueTO> list) {
        themeService.updateTagValue(list);
    }

    @PostMapping("/tag/update/single")
    void updateSingleTagValue(@RequestBody ThemeTagValueTO to) {
        themeService.updateSingleTagValue(to);
    }

    @GetMapping("/tag/value")
    List<ThemeTagValueTO> getTagValueList(@RequestParam("headerId") String headerId) {
        return themeService.getTagValueList(headerId);
    }

    @GetMapping("/top-custom/value")
    Map<String, String> findTopCustomValue(@RequestParam("headerId") String headerId) {
        return themeService.findTopCustomValue(headerId);
    }

    @PostMapping("/top-custom/update")
    void updateTopCustomValue(@RequestBody ThemeTopCustomValueTO topCustomValueTO) {
        themeService.updateTopCustomValue(topCustomValueTO);
    }

}
