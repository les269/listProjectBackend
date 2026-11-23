package com.lsb.listProjectBackend.repository.dynamic.search;

import com.lsb.listProjectBackend.entity.dynamic.search.SearchBookmarkItem;
import com.lsb.listProjectBackend.entity.dynamic.search.SearchBookmarkItemPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SearchBookmarkItemRepository extends JpaRepository<SearchBookmarkItem, SearchBookmarkItemPK> {
    List<SearchBookmarkItem> findAllById(String id);

    void deleteAllById(String id);

    @Query("SELECT COALESCE(MAX(item.seq), 0) FROM SearchBookmarkItem item WHERE item.id = :id")
    Integer findMaxSeqById(@Param("id") String id);
}
