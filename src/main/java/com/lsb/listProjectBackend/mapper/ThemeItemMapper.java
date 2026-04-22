package com.lsb.listProjectBackend.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.lsb.listProjectBackend.domain.ThemeItemTO;
import com.lsb.listProjectBackend.entity.dynamic.ThemeItem;

@Mapper
public interface ThemeItemMapper {
    ThemeItemMapper INSTANCE = Mappers.getMapper(ThemeItemMapper.class);

    ThemeItem toEntity(ThemeItemTO to);

    List<ThemeItem> toEntity(List<ThemeItemTO> list);

    ThemeItemTO toDomain(ThemeItem entity);

    List<ThemeItemTO> toDomain(List<ThemeItem> list);

    default JsonNode mapToJsonNode(String jsonString) {
        try {

            return (jsonString == null || jsonString.isEmpty())
                    ? null
                    : new ObjectMapper().readTree(jsonString);
        } catch (Exception e) {
            // ??閫??憭望?嚗??瘙???null ?征?拐辣
            return null;
        }
    }

    // ?芸?蝢抬?JsonNode 頧?String (摮?鞈?摨?
    default String mapToString(JsonNode node) {
        return (node == null) ? null : node.toString();
    }

}

