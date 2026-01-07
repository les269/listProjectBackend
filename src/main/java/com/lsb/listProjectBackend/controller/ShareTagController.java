package com.lsb.listProjectBackend.controller;

import com.lsb.listProjectBackend.domain.ShareTagTO;
import com.lsb.listProjectBackend.domain.ShareTagValueTO;
import com.lsb.listProjectBackend.service.ShareTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
public class ShareTagController {
    @Autowired
    private ShareTagService shareTagService;

    @GetMapping("/share-tag/all")
    List<ShareTagTO> getAllTag() {
        return shareTagService.getAllTag();
    }

    @PostMapping("/share-tag/add")
    void addTag(@RequestBody ShareTagTO req) {
        shareTagService.addTag(req);
    }

    @DeleteMapping("/share-tag/delete")
    void deleteTag(@RequestParam("shareTagId") String shareTagId) {
        shareTagService.deleteTag(shareTagId);
    }

    @GetMapping("/share-tag/in-use")
    boolean isShareTagInUse(@RequestParam("shareTagId") String shareTagId) {
        return shareTagService.hasThemeReference(shareTagId);
    }

    @PostMapping("/share-tag/value/by-ids")
    List<ShareTagValueTO> getShareTagValues(@RequestBody List<String> shareTagIds) {
        return shareTagService.findShareTagValues(shareTagIds);
    }

    @PostMapping("/share-tag/value/add")
    void addShareTagValue(@RequestBody ShareTagValueTO req) {
        shareTagService.addShareTagValue(req);
    }

    @DeleteMapping("/share-tag/value/delete")
    void deleteShareTagValue(@RequestParam("shareTagId") String shareTagId,
            @RequestParam("value") String value) {
        shareTagService.deleteShareTagValue(shareTagId, value);
    }
}
