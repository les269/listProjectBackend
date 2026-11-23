package com.lsb.listProjectBackend.service.search;

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
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SearchBookmarkServiceTest {
    private final SearchBookmarkRepository bookmarks = mock(SearchBookmarkRepository.class);
    private final SearchBookmarkItemRepository items = mock(SearchBookmarkItemRepository.class);
    private final SearchBookmarkService service = new SearchBookmarkServiceImpl(
            bookmarks, items, Mappers.getMapper(SearchBookmarkMapper.class));

    @Test
    void createsBookmarkWithoutAcceptingClientTimestamps() {
        when(bookmarks.findById("b1")).thenReturn(Optional.empty());
        when(bookmarks.saveAndFlush(any())).thenAnswer(call -> {
            SearchBookmark entity = call.getArgument(0);
            assertEquals("b1", entity.getId());
            assertEquals("Bookmark", entity.getName());
            assertNull(entity.getCreatedTime());
            assertNull(entity.getUpdatedTime());
            return entity;
        });
        var result = service.update(new SearchBookmarkTO("b1", "Bookmark",
                LocalDateTime.MIN, LocalDateTime.MIN));
        assertEquals("b1", result.id());
        verify(bookmarks).saveAndFlush(any());
    }

    @Test
    void updatesBookmarkWithoutOverwritingServerTimestamps() {
        SearchBookmark entity = new SearchBookmark();
        entity.setId("b1");
        entity.setName("Old");
        entity.setCreatedTime(LocalDateTime.of(2026, 1, 1, 0, 0));
        entity.setUpdatedTime(LocalDateTime.of(2026, 2, 1, 0, 0));
        when(bookmarks.findById("b1")).thenReturn(Optional.of(entity));
        when(bookmarks.saveAndFlush(entity)).thenReturn(entity);

        var result = service.update(new SearchBookmarkTO("b1", "New", LocalDateTime.MIN, LocalDateTime.MIN));
        assertEquals("New", result.name());
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.createdTime());
        assertEquals(LocalDateTime.of(2026, 2, 1, 0, 0), result.updatedTime());
    }

    @Test
    void rejectsItemWithoutParentBeforeSaving() {
        var error = assertThrows(LsbException.class,
                () -> service.updateItem(new SearchBookmarkItemTO("missing", "value", null, null, null)));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        verifyNoInteractions(items);
    }

    @Test
    void createsItemUsingSharedIdAndAllowsEmptySearchValue() {
        when(bookmarks.existsById("b1")).thenReturn(true);
        when(items.findById(new SearchBookmarkItemPK("b1", ""))).thenReturn(Optional.empty());
        when(items.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
        var result = service.updateItem(new SearchBookmarkItemTO("b1", "", 2, LocalDateTime.MIN, LocalDateTime.MIN));
        assertEquals("b1", result.id());
        assertEquals("", result.value());
        assertEquals(1, result.seq());
        assertNull(result.createdTime());
        assertNull(result.updatedTime());
    }

    @Test
    void updatesItemWithoutOverwritingCreationTime() {
        SearchBookmarkItem entity = new SearchBookmarkItem();
        entity.setId("b1");
        entity.setValue("new");
        entity.setSeq(1);
        entity.setCreatedTime(LocalDateTime.of(2026, 1, 1, 0, 0));
        when(bookmarks.existsById("b1")).thenReturn(true);
        when(items.findById(new SearchBookmarkItemPK("b1", "new"))).thenReturn(Optional.of(entity));
        when(items.saveAndFlush(entity)).thenReturn(entity);
        var result = service.updateItem(new SearchBookmarkItemTO("b1", "new", 3, LocalDateTime.MIN, null));
        assertEquals("new", result.value());
        assertEquals(3, entity.getSeq());
        assertEquals(3, result.seq());
        assertEquals(entity.getCreatedTime(), result.createdTime());
        assertNotEquals(LocalDateTime.MIN, result.createdTime());
    }

    @Test
    void appendsAfterMaximumSeqAndIgnoresRequestedSeqForNewItem() {
        when(bookmarks.existsById("b1")).thenReturn(true);
        when(items.findMaxSeqById("b1")).thenReturn(8);
        when(items.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
        var result = service.updateItem(new SearchBookmarkItemTO("b1", "added", 99, null, null));
        assertEquals(9, result.seq());
        verify(items).findMaxSeqById("b1");
    }

    @Test
    void preservesExistingSeqWhenRequestOmitsSeq() {
        SearchBookmarkItem entity = item("existing", 5);
        when(bookmarks.existsById("b1")).thenReturn(true);
        when(items.findById(new SearchBookmarkItemPK("b1", "existing"))).thenReturn(Optional.of(entity));
        when(items.saveAndFlush(entity)).thenReturn(entity);
        var result = service.updateItem(new SearchBookmarkItemTO("b1", "existing", null, null, null));
        assertEquals(5, result.seq());
        verify(items, never()).findMaxSeqById(anyString());
    }

    @Test
    void renumbersRemainingItemsInOriginalOrderWithNullSeqLast() {
        var first = item("first", 1);
        var third = item("third", 3);
        var last = item("last", null);
        when(items.findAllById("b1")).thenReturn(List.of(last, third, first));
        service.deleteItemById("b1", "deleted");
        assertEquals(1, first.getSeq());
        assertEquals(2, third.getSeq());
        assertEquals(3, last.getSeq());
        verify(items).saveAllAndFlush(List.of(third, last));
        verify(items).findAllById("b1");
    }

    private SearchBookmarkItem item(String value, Integer seq) {
        var item = new SearchBookmarkItem();
        item.setId("b1");
        item.setValue(value);
        item.setSeq(seq);
        return item;
    }

    @Test
    void updatesCompleteOrderAndReturnsItemsInRequestedOrder() {
        var first = item("first", 1);
        var second = item("second", 2);
        when(bookmarks.existsById("b1")).thenReturn(true);
        when(items.findAllById("b1")).thenReturn(List.of(first, second));
        var result = service.updateItemOrder(new SearchBookmarkOrderTO("b1", List.of("second", "first")));
        assertEquals(List.of("second", "first"), result.stream().map(SearchBookmarkItemTO::value).toList());
        assertEquals(List.of(1, 2), result.stream().map(SearchBookmarkItemTO::seq).toList());
        verify(items).saveAllAndFlush(List.of(second, first));
    }

    @Test
    void rejectsIncompleteOrUnknownOrderWithoutChangingAnyItem() {
        var first = item("first", 4);
        var second = item("second", 8);
        when(bookmarks.existsById("b1")).thenReturn(true);
        when(items.findAllById("b1")).thenReturn(List.of(first, second));
        for (var values : List.of(List.of("first"), List.of("first", "foreign"),
                List.of("first", "second", "extra"), List.<String>of())) {
            var error = assertThrows(LsbException.class,
                    () -> service.updateItemOrder(new SearchBookmarkOrderTO("b1", values)));
            assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
            assertEquals(4, first.getSeq());
            assertEquals(8, second.getSeq());
        }
        verify(items, never()).saveAllAndFlush(any());
    }

    @Test
    void rejectsInvalidOrderRequestBeforeDatabaseAccess() {
        assertThrows(LsbException.class, () -> service.updateItemOrder(null));
        assertThrows(LsbException.class, () -> service.updateItemOrder(new SearchBookmarkOrderTO(" ", List.of())));
        assertThrows(LsbException.class, () -> service.updateItemOrder(new SearchBookmarkOrderTO("b1", null)));
        assertThrows(LsbException.class,
                () -> service.updateItemOrder(new SearchBookmarkOrderTO("b1", List.of("first", "first"))));
        assertThrows(LsbException.class,
                () -> service.updateItemOrder(new SearchBookmarkOrderTO("b1", java.util.Arrays.asList("first", null))));
        verifyNoInteractions(bookmarks, items);
    }

    @Test
    void acceptsEmptyOrderOnlyForExistingEmptyBookmark() {
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(LsbException.class,
                () -> service.updateItemOrder(new SearchBookmarkOrderTO("b1", List.of()))).getStatus());
        when(bookmarks.existsById("b1")).thenReturn(true);
        assertTrue(service.updateItemOrder(new SearchBookmarkOrderTO("b1", List.of())).isEmpty());
        verify(items, never()).saveAllAndFlush(any());
    }

    @Test
    void acceptsEmptyStringValueAndDoesNotWriteUnchangedOrder() {
        when(bookmarks.existsById("b1")).thenReturn(true);
        when(items.findAllById("b1")).thenReturn(List.of(item("", 1)));
        var result = service.updateItemOrder(new SearchBookmarkOrderTO("b1", List.of("")));
        assertEquals("", result.getFirst().value());
        assertEquals(1, result.getFirst().seq());
        verify(items, never()).saveAllAndFlush(any());
    }

    @Test
    void deletesChildBeforeParentAndDoesNotDeleteParentWhenDeletingOnlyItem() {
        service.deleteById("b1");
        var order = inOrder(items, bookmarks);
        order.verify(items).deleteAllById("b1");
        order.verify(items).flush();
        order.verify(bookmarks).deleteById("b1");
        order.verify(bookmarks).flush();

        clearInvocations(items, bookmarks);
        service.deleteItemById("b1", "value");
        verify(items).deleteById(new SearchBookmarkItemPK("b1", "value"));
        verify(items, never()).deleteAllById(anyString());
        verifyNoInteractions(bookmarks);
    }

    @Test
    void deletesEmptySearchValueByCompositeKey() {
        service.deleteItemById("b1", "");
        verify(items).deleteById(new SearchBookmarkItemPK("b1", ""));
        verify(items, never()).deleteAllById(anyString());
        verifyNoInteractions(bookmarks);
    }

    @Test
    void readsListsAndFindsAllItemsById() {
        SearchBookmark bookmark = new SearchBookmark();
        bookmark.setId("b1");
        SearchBookmarkItem item = new SearchBookmarkItem();
        item.setId("b1");
        item.setValue("value");
        item.setSeq(4);
        when(bookmarks.findAll()).thenReturn(List.of(bookmark));
        when(bookmarks.findById("b1")).thenReturn(Optional.of(bookmark));
        when(items.findAll()).thenReturn(List.of(item));
        SearchBookmarkItem second = new SearchBookmarkItem();
        second.setId("b1");
        second.setValue("second");
        when(items.findAllById("b1")).thenReturn(List.of(item, second));
        assertEquals("b1", service.getAll().getFirst().id());
        assertEquals("b1", service.getById("b1").id());
        assertEquals("value", service.getAllItems().getFirst().value());
        assertEquals(4, service.getAllItems().getFirst().seq());
        var result = service.getItemById("b1");
        assertEquals(4, result.getFirst().seq());
        assertEquals(List.of("value", "second"),
                result.stream().map(SearchBookmarkItemTO::value).toList());
        verify(items).findAllById("b1");
    }

    @Test
    void returnsNotFoundForMissingRecords() {
        assertEquals(HttpStatus.NOT_FOUND,
                assertThrows(LsbException.class, () -> service.getById("missing")).getStatus());
        assertTrue(service.getItemById("missing").isEmpty());
    }

    @Test
    void rejectsInvalidInputBeforeDatabaseAccess() {
        assertThrows(LsbException.class, () -> service.update(null));
        assertThrows(LsbException.class, () -> service.update(new SearchBookmarkTO("b1", " ", null, null)));
        assertThrows(LsbException.class, () -> service.update(new SearchBookmarkTO(" ", "name", null, null)));
        assertThrows(LsbException.class, () -> service.updateItem(null));
        assertThrows(LsbException.class, () -> service.updateItem(new SearchBookmarkItemTO("b1", null, null, null, null)));
        assertThrows(LsbException.class, () -> service.updateItem(new SearchBookmarkItemTO(" ", "value", null, null, null)));
        assertThrows(LsbException.class, () -> service.getById(" "));
        assertThrows(LsbException.class, () -> service.getItemById(" "));
        assertThrows(LsbException.class, () -> service.deleteById(null));
        assertThrows(LsbException.class, () -> service.deleteItemById(" ", "value"));
        assertThrows(LsbException.class, () -> service.deleteItemById("b1", null));
        verifyNoInteractions(bookmarks, items);
    }
}
