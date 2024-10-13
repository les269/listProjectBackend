package com.lsb.listProjectBackend.domain;

import lombok.Data;

import java.util.List;

@Data
public class ThemeCustomValueRequest {
    private String headerId;
    private List<String> valueList;
}
