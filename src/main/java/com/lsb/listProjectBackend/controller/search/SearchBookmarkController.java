package com.lsb.listProjectBackend.controller.search;

import com.lsb.listProjectBackend.domain.search.SearchBookmarkTO;
import com.lsb.listProjectBackend.domain.search.SearchBookmarkItemTO;
import com.lsb.listProjectBackend.domain.search.SearchBookmarkOrderTO;
import com.lsb.listProjectBackend.service.search.SearchBookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/search-bookmark")
@RequiredArgsConstructor
public class SearchBookmarkController {
    private final SearchBookmarkService service;

    @GetMapping("/all")
    public List<SearchBookmarkTO> getAll() {
        return service.getAll();
    }

    @GetMapping("/get")
    public SearchBookmarkTO getById(@RequestParam("id") String id) {
        return service.getById(id);
    }

    @PostMapping("/update")
    public SearchBookmarkTO update(@RequestBody SearchBookmarkTO request) {
        return service.update(request);
    }

    @DeleteMapping("/delete")
    public void deleteById(@RequestParam("id") String id) {
        service.deleteById(id);
    }

    @GetMapping("/item/all")
    public List<SearchBookmarkItemTO> getAllItems() {
        return service.getAllItems();
    }

    @GetMapping("/item/get")
    public List<SearchBookmarkItemTO> getItemById(@RequestParam("id") String id) {
        return service.getItemById(id);
    }

    @PostMapping("/item/update")
    public SearchBookmarkItemTO updateItem(@RequestBody SearchBookmarkItemTO request) {
        return service.updateItem(request);
    }

    @DeleteMapping("/item/delete")
    public void deleteItemById(@RequestParam("id") String id, @RequestParam("value") String value) {
        service.deleteItemById(id, value);
    }

    @PutMapping("/item/order")
    public List<SearchBookmarkItemTO> updateItemOrder(@RequestBody SearchBookmarkOrderTO request) {
        return service.updateItemOrder(request);
    }
}
