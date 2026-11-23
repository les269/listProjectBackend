package com.lsb.listProjectBackend.controller.common;

import com.lsb.listProjectBackend.domain.common.HeadersMapTO;
import com.lsb.listProjectBackend.service.common.HeadersMapService;
import com.lsb.listProjectBackend.utils.Global;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/headers/map")
@RequiredArgsConstructor
public class HeadersMapController {
    private final HeadersMapService headersMapService;

    @GetMapping("/by-id-and-type")
    public HeadersMapTO getMapByIdAndType(@RequestParam("refId") String refId,
            @RequestParam("type") Global.HeadersMapType type) {
        return headersMapService.getMapByIdAndType(refId, type);
    }

    @GetMapping("/by-ref-id")
    public List<HeadersMapTO> getMapByRefId(@RequestParam("refId") String refId) {
        return headersMapService.getMapByRefId(refId);
    }

    @GetMapping("/by-type")
    public List<HeadersMapTO> getMapByType(@RequestParam("type") Global.HeadersMapType type) {
        return headersMapService.getMapByType(type);
    }

    @PostMapping("/update")
    public void updateMap(@RequestBody HeadersMapTO req) {
        headersMapService.updateMap(req);
    }

    @DeleteMapping("/delete")
    public void deleteMap(@RequestParam("refId") String refId,
            @RequestParam("type") Global.HeadersMapType type) {
        headersMapService.deleteMap(refId, type);
    }

    @GetMapping("/in-use")
    public boolean isInUseMap(@RequestParam("headersId") String headersId,
            @RequestParam("type") Global.HeadersMapType type) {
        return headersMapService.isInUseMap(headersId, type);
    }

    @GetMapping("/headers-in-use")
    public boolean headersIsInUse(@RequestParam("headersId") String headersId) {
        return headersMapService.headersIsInUse(headersId);
    }
}
