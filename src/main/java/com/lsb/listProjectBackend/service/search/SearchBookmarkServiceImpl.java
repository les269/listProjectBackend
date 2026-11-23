package com.lsb.listProjectBackend.service.search;

import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.aop.UseDynamicTx;
import com.lsb.listProjectBackend.domain.common.LsbException;
import com.lsb.listProjectBackend.domain.search.SearchBookmarkTO;
import com.lsb.listProjectBackend.domain.search.SearchBookmarkItemTO;
import com.lsb.listProjectBackend.domain.search.SearchBookmarkOrderTO;
import com.lsb.listProjectBackend.entity.dynamic.search.SearchBookmark;
import com.lsb.listProjectBackend.entity.dynamic.search.SearchBookmarkItem;
import com.lsb.listProjectBackend.entity.dynamic.search.SearchBookmarkItemPK;
import com.lsb.listProjectBackend.mapper.search.SearchBookmarkMapper;
import com.lsb.listProjectBackend.repository.dynamic.search.SearchBookmarkRepository;
import com.lsb.listProjectBackend.repository.dynamic.search.SearchBookmarkItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.stream.Collectors;

@UseDynamic
@UseDynamicTx
@Service
@RequiredArgsConstructor
public class SearchBookmarkServiceImpl implements SearchBookmarkService {
    private final SearchBookmarkRepository bookmarkRepository;
    private final SearchBookmarkItemRepository itemRepository;
    private final SearchBookmarkMapper mapper;

    @Override
    public List<SearchBookmarkTO> getAll() {
        return mapper.toDomainList(bookmarkRepository.findAll());
    }

    @Override
    public SearchBookmarkTO getById(String id) {
        requireId(id);
        return mapper.toDomain(bookmarkRepository.findById(id)
                .orElseThrow(() -> new LsbException("Search bookmark not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public SearchBookmarkTO update(SearchBookmarkTO request) {
        if (request == null || !StringUtils.hasText(request.name())) {
            throw new LsbException("Search bookmark name is required");
        }
        requireId(request.id());
        SearchBookmark entity = bookmarkRepository.findById(request.id()).orElseGet(() -> {
            SearchBookmark bookmark = new SearchBookmark();
            bookmark.setId(request.id());
            return bookmark;
        });
        mapper.update(request, entity);
        return mapper.toDomain(bookmarkRepository.saveAndFlush(entity));
    }

    @Override
    public void deleteById(String id) {
        requireId(id);
        // Flush child deletion before deleting the parent to respect the shared-ID foreign key.
        itemRepository.deleteAllById(id);
        itemRepository.flush();
        bookmarkRepository.deleteById(id);
        bookmarkRepository.flush();
    }

    @Override
    public List<SearchBookmarkItemTO> getAllItems() {
        return mapper.toItemDomainList(itemRepository.findAll());
    }

    @Override
    public List<SearchBookmarkItemTO> getItemById(String id) {
        requireId(id);
        return mapper.toItemDomainList(getOrderedItems(id));
    }

    @Override
    public SearchBookmarkItemTO updateItem(SearchBookmarkItemTO request) {
        if (request == null || request.value() == null) {
            throw new LsbException("Search bookmark value is required");
        }
        requireId(request.id());
        if (!bookmarkRepository.existsById(request.id())) {
            throw new LsbException("Search bookmark not found", HttpStatus.NOT_FOUND);
        }
        SearchBookmarkItemPK key = new SearchBookmarkItemPK(request.id(), request.value());
        var existing = itemRepository.findById(key);
        SearchBookmarkItem entity = existing.orElseGet(() -> {
            SearchBookmarkItem item = new SearchBookmarkItem();
            item.setId(request.id());
            return item;
        });
        Integer previousSeq = entity.getSeq();
        mapper.update(request, entity);
        if (existing.isEmpty()) {
            entity.setSeq(Math.addExact(itemRepository.findMaxSeqById(request.id()), 1));
        } else if (request.seq() == null) {
            entity.setSeq(previousSeq);
        }
        return mapper.toDomain(itemRepository.saveAndFlush(entity));
    }

    @Override
    public void deleteItemById(String id, String value) {
        requireId(id);
        if (value == null) {
            throw new LsbException("Search bookmark value is required");
        }
        itemRepository.deleteById(new SearchBookmarkItemPK(id, value));
        itemRepository.flush();
        saveItemOrder(getOrderedItems(id));
    }

    @Override
    public List<SearchBookmarkItemTO> updateItemOrder(SearchBookmarkOrderTO request) {
        if (request == null) {
            throw new LsbException("Search bookmark order is required");
        }
        requireId(request.id());
        if (request.values() == null) {
            throw new LsbException("Search bookmark values are required");
        }
        var values = new HashSet<>(request.values());
        if (values.contains(null) || values.size() != request.values().size()) {
            throw new LsbException("Search bookmark values must be non-null and unique");
        }
        if (!bookmarkRepository.existsById(request.id())) {
            throw new LsbException("Search bookmark not found", HttpStatus.NOT_FOUND);
        }
        List<SearchBookmarkItem> items = itemRepository.findAllById(request.id());
        var itemsByValue = items.stream()
                .collect(Collectors.toMap(item -> item.getValue(), item -> item));
        if (!itemsByValue.keySet().equals(values)) {
            throw new LsbException("All bookmark values must be provided exactly once");
        }
        List<SearchBookmarkItem> ordered = request.values().stream().map(itemsByValue::get).toList();
        saveItemOrder(ordered);
        return mapper.toItemDomainList(ordered);
    }

    private void saveItemOrder(List<SearchBookmarkItem> ordered) {
        List<SearchBookmarkItem> changed = new ArrayList<>();
        int seq = 1;
        for (SearchBookmarkItem item : ordered) {
            if (!Integer.valueOf(seq).equals(item.getSeq())) {
                item.setSeq(seq);
                changed.add(item);
            }
            seq++;
        }
        if (!changed.isEmpty()) {
            itemRepository.saveAllAndFlush(changed);
        }
    }

    private List<SearchBookmarkItem> getOrderedItems(String id) {
        return itemRepository.findAllById(id).stream()
                .sorted(Comparator.comparing(SearchBookmarkItem::getSeq, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(SearchBookmarkItem::getValue))
                .toList();
    }

    private void requireId(String id) {
        if (!StringUtils.hasText(id)) {
            throw new LsbException("Search bookmark ID is required");
        }
    }
}
