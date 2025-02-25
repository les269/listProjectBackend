package com.lsb.listProjectBackend.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class CssSelect {
    private Integer seq;
    private String key;
    private String value;
    private String replaceString;
    private String attr;
    private boolean convertToArray;
    private boolean onlyOwn;
    private String replaceRegular;
    private String replaceRegularTo;
    private String replaceValueMapName;
    private String splitText;
}
