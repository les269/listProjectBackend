package com.lsb.listProjectBackend.controller.theme;

import com.lsb.listProjectBackend.domain.theme.CopyThemeItemReqTO;
import com.lsb.listProjectBackend.domain.theme.ThemeItemTO;
import com.lsb.listProjectBackend.service.theme.ThemeItemService;
import com.lsb.listProjectBackend.utils.Global;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/theme/item")
@RequiredArgsConstructor
public class ThemeItemController {
    private final ThemeItemService themeItemService;

    @GetMapping
    ThemeItemTO getThemeItemById(@RequestParam("type") Global.ThemeItemType type,
            @RequestParam("itemId") String itemId) {
        return themeItemService.getThemeItemById(type, itemId);
    }

    @GetMapping("/all")
    List<ThemeItemTO> getAllThemeItem(@RequestParam("type") Global.ThemeItemType type) {
        return themeItemService.getAllThemeItem(type);
    }

    @GetMapping("/by-type")
    List<ThemeItemTO> getThemeItemByType(@RequestParam("type") Global.ThemeItemType type) {
        return themeItemService.getAllThemeItem(type);
    }

    @GetMapping("/header-id")
    List<ThemeItemTO> getItemsByHeaderId(@RequestParam("headerId") String headerId) {
        return themeItemService.getItemsByHeaderId(headerId);
    }

    @PostMapping("/update")
    void updateThemeItem(@RequestBody ThemeItemTO req) {
        themeItemService.updateThemeItem(req);
    }

    @PostMapping("/copy")
    void copyThemeItem(@RequestBody CopyThemeItemReqTO req) {
        themeItemService.copyThemeItem(req);
    }

    @DeleteMapping("/delete")
    void deleteThemeItem(@RequestParam("type") Global.ThemeItemType type, @RequestParam("itemId") String itemId) {
        themeItemService.deleteThemeItem(type, itemId);
    }
}
