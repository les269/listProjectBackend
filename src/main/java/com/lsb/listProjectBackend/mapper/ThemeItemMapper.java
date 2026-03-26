package com.lsb.listProjectBackend.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
            // 處理解析失敗，視需求回傳 null 或空物件
            return null;
        }
    }

    // 自定義：JsonNode 轉 String (存回資料庫)
    default String mapToString(JsonNode node) {
        return (node == null) ? null : node.toString();
    }

}
