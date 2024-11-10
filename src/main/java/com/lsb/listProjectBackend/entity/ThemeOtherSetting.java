package com.lsb.listProjectBackend.entity;

import lombok.Data;

import java.util.List;

@Data
public class ThemeOtherSetting {
    private List<String> rowColor;
    private Integer listPageSize;
    private boolean showDuplicate;
    private List<ThemeTopCustom> themeTopCustomList;
}