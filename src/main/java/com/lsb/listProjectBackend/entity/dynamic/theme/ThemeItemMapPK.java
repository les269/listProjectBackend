package com.lsb.listProjectBackend.entity.dynamic.theme;

import com.lsb.listProjectBackend.utils.Global.ThemeItemType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThemeItemMapPK {
    private String headerId;
    private ThemeItemType type;
}
