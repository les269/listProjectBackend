package com.lsb.listProjectBackend.domain;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.lsb.listProjectBackend.utils.Global.ThemeLabelType;
import lombok.Data;

@Data
public class ThemeLabelTO {
    private String seq;
    private String byKey;
    private String label;
    private ThemeLabelType type;
    private String splitBy;
    private String useSpace;
    @JsonProperty("isSearchButton")
    private boolean isSearchButton;
    @JsonProperty("isSearchValue")
    private boolean isSearchValue;
    @JsonProperty("isCopy")
    private boolean isCopy;
    @JsonProperty("isVisible")
    private boolean isVisible;
    @JsonProperty("isSort")
    private boolean isSort;
    @JsonProperty("isDefaultKey")
    private boolean isDefaultKey;

}
