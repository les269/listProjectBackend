package com.lsb.listProjectBackend.controller;

import com.lsb.listProjectBackend.domain.CopyThemeItemReq;
import com.lsb.listProjectBackend.domain.ThemeItemTO;
import com.lsb.listProjectBackend.service.ThemeItemService;
import com.lsb.listProjectBackend.utils.Global;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
public class ThemeItemController {
    @Autowired
    private ThemeItemService themeItemService;

    @GetMapping("/theme/item")
    ThemeItemTO getThemeItemById(@RequestParam("type") Global.ThemeItemType type,
            @RequestParam("itemId") String itemId) {
        return themeItemService.getThemeItemById(type, itemId);
    }

    @GetMapping("/theme/item/all")
    List<ThemeItemTO> getAllThemeItem(@RequestParam("type") Global.ThemeItemType type) {
        return themeItemService.getAllThemeItem(type);
    }

    @GetMapping("/theme/item/by-type")
    List<ThemeItemTO> getThemeItemByType(@RequestParam("type") Global.ThemeItemType type) {
        return themeItemService.getAllThemeItem(type);
    }

    @GetMapping("/theme/item/header-id")
    List<ThemeItemTO> getItemsByHeaderId(@RequestParam("headerId") String headerId) {
        return themeItemService.getItemsByHeaderId(headerId);
    }

    @PostMapping("/theme/item/update")
    void updateThemeItem(@RequestBody ThemeItemTO req) {
        themeItemService.updateThemeItem(req);
    }

    @PostMapping("/theme/item/copy")
    void copyThemeItem(@RequestBody CopyThemeItemReq req) {
        themeItemService.copyThemeItem(req);
    }

    @DeleteMapping("/theme/item/delete")
    void deleteThemeItem(@RequestParam("type") Global.ThemeItemType type, @RequestParam("itemId") String itemId) {
        themeItemService.deleteThemeItem(type, itemId);
    }
}
