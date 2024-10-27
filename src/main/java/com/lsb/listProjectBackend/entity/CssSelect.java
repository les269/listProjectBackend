package com.lsb.listProjectBackend.entity;

import lombok.Data;

@Data
public class CssSelect {
    private String key;
    private String value;
    private String replaceString;
    private String attr;
    private boolean convertToArray;
}
