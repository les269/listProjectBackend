package com.lsb.listProjectBackend.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lsb.listProjectBackend.utils.Global;
import lombok.Data;

@Data
public class ThemeDBTO {
    private Global.ThemeDBType type;
    private String source;
    private String label;
    private String groups;
    private String seq;
    @JsonProperty("isDefault")
    private boolean isDefault;

}
