package com.lsb.listProjectBackend.entity.dynamic.spider;

import lombok.Data;

@Data
public class ExtractionCondition {
    private String key;
    private String value;
    private boolean ignoreCase;
}
