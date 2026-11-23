package com.lsb.listProjectBackend.controller.theme;

import com.lsb.listProjectBackend.domain.theme.ThemeItemMapTO;
import com.lsb.listProjectBackend.service.theme.ThemeItemService;
import com.lsb.listProjectBackend.utils.Global;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/theme/item/map")
@RequiredArgsConstructor
public class ThemeItemMapController {
    private final ThemeItemService themeItemService;

    @GetMapping("/by-type")
    List<ThemeItemMapTO> getThemeItemMapByType(@RequestParam("type") Global.ThemeItemType type) {
        return themeItemService.getAllThemeItemMapByType(type);
    }

    @PostMapping("/update")
    void updateThemeItemMap(@RequestBody ThemeItemMapTO req) {
        themeItemService.updateThemeItemMap(req);
    }

    @DeleteMapping("/delete")
    void deleteThemeItemMap(@RequestParam("type") Global.ThemeItemType type,
            @RequestParam("headerId") String headerId) {
        themeItemService.deleteThemeItemMap(type, headerId);
    }

    @GetMapping("/in-use")
    boolean themeItemMapInUse(@RequestParam("type") Global.ThemeItemType type,
            @RequestParam("itemId") String itemId) {
        return themeItemService.themeItemMapInUse(type, itemId);
    }
}
