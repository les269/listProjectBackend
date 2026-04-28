package com.lsb.listProjectBackend.controller.spider;

import com.lsb.listProjectBackend.domain.spider.SpiderItemTO;
import com.lsb.listProjectBackend.service.spider.SpiderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
public class SpiderItemController {

    @Autowired
    private SpiderItemService spiderItemService;

    @GetMapping("/spider-item/all")
    public List<SpiderItemTO> getAllItem() {
        return spiderItemService.getAll();
    }

    @GetMapping("/spider-item")
    public SpiderItemTO getItem(@RequestParam("spiderItemId") String spiderItemId) {
        return spiderItemService.getById(spiderItemId);
    }

    @GetMapping("/spider-item/by-spider-id")
    public List<SpiderItemTO> getItemBySpiderId(@RequestParam("spiderId") String spiderId) {
        return spiderItemService.getBySpiderId(spiderId);
    }

    @PostMapping("/spider-item/update")
    public void updateItem(@RequestBody SpiderItemTO req) {
        spiderItemService.update(req);
    }

    @DeleteMapping("/spider-item/delete")
    public void deleteItem(@RequestParam("spiderItemId") String spiderItemId) {
        spiderItemService.delete(spiderItemId);
    }

    @GetMapping("/spider-item/in-use")
    public boolean isItemInUse(@RequestParam("spiderItemId") String spiderItemId) {
        return spiderItemService.exists(spiderItemId);
    }

    @PostMapping("/spider-item/by-id-list")
    public List<SpiderItemTO> getItemByIdList(@RequestBody List<String> spiderItemIdList) {
        return spiderItemService.getByIdList(spiderItemIdList);
    }
}