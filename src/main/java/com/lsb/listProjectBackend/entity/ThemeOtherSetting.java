package com.lsb.listProjectBackend.entity;

import com.lsb.listProjectBackend.converter.ThemeCustomListConverter;
import jakarta.persistence.Convert;
import lombok.Data;

import java.util.List;

@Data
public class ThemeOtherSetting {
    private List<String> rowColor;
    private Integer listPageSize;
    @Convert(converter = ThemeCustomListConverter.class)
    private List<ThemeCustom> topCustomList;
}
