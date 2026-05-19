package com.lsb.listProjectBackend.controller.common;

import com.lsb.listProjectBackend.domain.common.HeadersTO;
import com.lsb.listProjectBackend.service.common.HeadersService;
import com.lsb.listProjectBackend.utils.Global;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class HeadersController {
    private final HeadersService headersService;

    @GetMapping("/headers/by-ref-id-and-type")
    public HeadersTO getByRefIdAndType(@RequestParam("refId") String refId,
            @RequestParam("type") Global.HeadersMapType type) {
        return headersService.getByRefIdAndType(refId, type);
    }

    @GetMapping("/headers/all")
    public List<HeadersTO> getAll() {
        return headersService.getAll();
    }

    @GetMapping("/headers")
    public HeadersTO getByHeadersId(@RequestParam("headersId") String headersId) {
        return headersService.getByHeadersId(headersId);
    }

    @PostMapping("/headers/update")
    public void update(@RequestBody HeadersTO req) {
        headersService.update(req);
    }

    @DeleteMapping("/headers/delete")
    public void delete(@RequestParam("headersId") String headersId) {
        headersService.delete(headersId);
    }
}
