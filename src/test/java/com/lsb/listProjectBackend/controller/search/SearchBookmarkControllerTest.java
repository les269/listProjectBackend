package com.lsb.listProjectBackend.controller.search;

import com.lsb.listProjectBackend.config.GlobalDefaultExceptionHandler;
import com.lsb.listProjectBackend.domain.common.LsbException;
import com.lsb.listProjectBackend.domain.search.SearchBookmarkItemTO;
import com.lsb.listProjectBackend.domain.search.SearchBookmarkOrderTO;
import com.lsb.listProjectBackend.service.search.SearchBookmarkService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SearchBookmarkControllerTest {
    private final SearchBookmarkService service = mock(SearchBookmarkService.class);

    @Test
    void acceptsCompleteOrderAndReturnsOrderedItems() throws Exception {
        var request = new SearchBookmarkOrderTO("b1", List.of("second", "first"));
        when(service.updateItemOrder(request)).thenReturn(List.of(
                new SearchBookmarkItemTO("b1", "second", 1, null, null),
                new SearchBookmarkItemTO("b1", "first", 2, null, null)));
        var mvc = MockMvcBuilders.standaloneSetup(new SearchBookmarkController(service)).build();
        mvc.perform(put("/api/search-bookmark/item/order").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"b1","values":["second","first"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].value").value("second"))
                .andExpect(jsonPath("$[0].seq").value(1))
                .andExpect(jsonPath("$[1].seq").value(2));
        verify(service).updateItemOrder(request);
    }

    @Test
    void returnsBadRequestWhenOrderValidationFails() throws Exception {
        when(service.updateItemOrder(any())).thenThrow(new LsbException("Invalid order"));
        var mvc = MockMvcBuilders.standaloneSetup(new SearchBookmarkController(service))
                .setControllerAdvice(new GlobalDefaultExceptionHandler()).build();
        mvc.perform(put("/api/search-bookmark/item/order").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"b1","values":["first","first"]}
                                """))
                .andExpect(status().isBadRequest());
    }
}
