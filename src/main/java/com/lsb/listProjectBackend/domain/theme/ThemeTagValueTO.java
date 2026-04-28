package com.lsb.listProjectBackend.domain.theme;

import lombok.Data;

import java.util.List;

@Data
public class ThemeTagValueTO {
    private String headerId;
    private String tag;
    private List<String> valueList;
}
