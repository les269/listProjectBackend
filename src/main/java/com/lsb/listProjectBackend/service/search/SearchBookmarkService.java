package com.lsb.listProjectBackend.service.search;

import com.lsb.listProjectBackend.domain.search.SearchBookmarkTO;
import com.lsb.listProjectBackend.domain.search.SearchBookmarkItemTO;
import com.lsb.listProjectBackend.domain.search.SearchBookmarkOrderTO;

import java.util.List;

public interface SearchBookmarkService {
    List<SearchBookmarkTO> getAll();

    SearchBookmarkTO getById(String id);

    SearchBookmarkTO update(SearchBookmarkTO request);

    void deleteById(String id);

    List<SearchBookmarkItemTO> getAllItems();

    List<SearchBookmarkItemTO> getItemById(String id);

    SearchBookmarkItemTO updateItem(SearchBookmarkItemTO request);

    void deleteItemById(String id, String value);

    List<SearchBookmarkItemTO> updateItemOrder(SearchBookmarkOrderTO request);
}
