package com.lsb.listProjectBackend.repository.dynamic.search;

import com.lsb.listProjectBackend.entity.dynamic.search.SearchBookmark;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchBookmarkRepository extends JpaRepository<SearchBookmark, String> {
}
