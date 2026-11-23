package com.lsb.listProjectBackend.controller.common;

import com.lsb.listProjectBackend.domain.common.HeadersTO;
import com.lsb.listProjectBackend.service.common.HeadersService;
import com.lsb.listProjectBackend.utils.Global;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/headers")
@RequiredArgsConstructor
public class HeadersController {
    private final HeadersService headersService;

    @GetMapping("/by-ref-id-and-type")
    public HeadersTO getByRefIdAndType(@RequestParam("refId") String refId,
            @RequestParam("type") Global.HeadersMapType type) {
        return headersService.getByRefIdAndType(refId, type);
    }

    @GetMapping("/all")
    public List<HeadersTO> getAll() {
        return headersService.getAll();
    }

    @GetMapping
    public HeadersTO getByHeadersId(@RequestParam("headersId") String headersId) {
        return headersService.getByHeadersId(headersId);
    }

    @PostMapping("/update")
    public void update(@RequestBody HeadersTO req) {
        headersService.update(req);
    }

    @DeleteMapping("/delete")
    public void delete(@RequestParam("headersId") String headersId) {
        headersService.delete(headersId);
    }
}
