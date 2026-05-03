package com.lsb.listProjectBackend.controller.theme;

import com.lsb.listProjectBackend.domain.theme.ThemeItemMapTO;
import com.lsb.listProjectBackend.service.theme.ThemeItemService;
import com.lsb.listProjectBackend.utils.Global;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class ThemeItemMapController {
    private final ThemeItemService themeItemService;

    @GetMapping("/theme/item/map/by-type")
    List<ThemeItemMapTO> getThemeItemMapByType(@RequestParam("type") Global.ThemeItemType type) {
        return themeItemService.getAllThemeItemMapByType(type);
    }

    @PostMapping("/theme/item/map/update")
    void updateThemeItemMap(@RequestBody ThemeItemMapTO req) {
        themeItemService.updateThemeItemMap(req);
    }

    @DeleteMapping("/theme/item/map/delete")
    void deleteThemeItemMap(@RequestParam("type") Global.ThemeItemType type,
            @RequestParam("headerId") String headerId) {
        themeItemService.deleteThemeItemMap(type, headerId);
    }

    @GetMapping("/theme/item/map/in-use")
    boolean themeItemMapInUse(@RequestParam("type") Global.ThemeItemType type,
            @RequestParam("itemId") String itemId) {
        return themeItemService.themeItemMapInUse(type, itemId);
    }
}
