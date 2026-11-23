package com.lsb.listProjectBackend.controller.share;

import com.lsb.listProjectBackend.domain.share.ShareTagTO;
import com.lsb.listProjectBackend.domain.share.ShareTagValueDeleteListTO;
import com.lsb.listProjectBackend.domain.share.ShareTagValueTO;
import com.lsb.listProjectBackend.service.share.ShareTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/share-tag")
@RequiredArgsConstructor
public class ShareTagController {
    private final ShareTagService shareTagService;

    @GetMapping("/all")
    List<ShareTagTO> getAllTag() {
        return shareTagService.getAllTag();
    }

    @PostMapping("/add")
    void addTag(@RequestBody ShareTagTO req) {
        shareTagService.addTag(req);
    }

    @DeleteMapping("/delete")
    void deleteTag(@RequestParam("shareTagId") String shareTagId) {
        shareTagService.deleteTag(shareTagId);
    }

    @GetMapping("/in-use")
    List<String> getThemeHeaderIdsByShareTagId(@RequestParam("shareTagId") String shareTagId) {
        return shareTagService.getThemeHeaderIdsByShareTagId(shareTagId);
    }

    @PostMapping("/value/by-ids")
    List<ShareTagValueTO> getShareTagValues(@RequestBody List<String> shareTagIds) {
        return shareTagService.findShareTagValues(shareTagIds);
    }

    @GetMapping("/value/id")
    List<ShareTagValueTO> getShareTagValueById(@RequestParam("shareTagId") String shareTagId) {
        return shareTagService.findShareTagValueById(shareTagId);
    }

    @PostMapping("/value/add")
    void addShareTagValue(@RequestBody ShareTagValueTO req) {
        shareTagService.addShareTagValue(req);
    }

    @DeleteMapping("/value/delete")
    void deleteShareTagValue(@RequestParam("shareTagId") String shareTagId,
            @RequestParam("value") String value) {
        shareTagService.deleteShareTagValue(shareTagId, value);
    }

    @DeleteMapping("/value/delete-list")
    void deleteShareTagValueList(@RequestBody ShareTagValueDeleteListTO req) {
        shareTagService.deleteShareTagValueList(req);
    }
}
