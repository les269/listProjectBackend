package com.lsb.listProjectBackend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lsb.listProjectBackend.utils.Global.ThemeLabelType;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ThemeLabel  {
    @Enumerated(EnumType.STRING)
    private ThemeLabelType type;
    private String seq;
    private String byKey;
    private String label;
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
