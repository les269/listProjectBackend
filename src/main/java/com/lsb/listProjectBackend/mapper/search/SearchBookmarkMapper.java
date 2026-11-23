package com.lsb.listProjectBackend.mapper.search;

import com.lsb.listProjectBackend.domain.search.SearchBookmarkTO;
import com.lsb.listProjectBackend.domain.search.SearchBookmarkItemTO;
import com.lsb.listProjectBackend.entity.dynamic.search.SearchBookmark;
import com.lsb.listProjectBackend.entity.dynamic.search.SearchBookmarkItem;
import com.lsb.listProjectBackend.mapper.SpringAndIgnoreUnmappedMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(config = SpringAndIgnoreUnmappedMapperConfig.class)
public interface SearchBookmarkMapper {
    SearchBookmarkTO toDomain(SearchBookmark entity);

    SearchBookmarkItemTO toDomain(SearchBookmarkItem entity);

    List<SearchBookmarkTO> toDomainList(List<SearchBookmark> entities);

    List<SearchBookmarkItemTO> toItemDomainList(List<SearchBookmarkItem> entities);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "name")
    void update(SearchBookmarkTO source, @MappingTarget SearchBookmark target);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "value", source = "value")
    @Mapping(target = "seq", source = "seq")
    void update(SearchBookmarkItemTO source, @MappingTarget SearchBookmarkItem target);
}
