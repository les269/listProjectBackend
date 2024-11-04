package com.lsb.listProjectBackend.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CssSelect {
    private String key;
    private String value;
    private String replaceString;
    private String attr;
    private boolean convertToArray;
    private boolean onlyOwn;
    private String replaceRegular = "";
    private String replaceRegularTo = "";
}
