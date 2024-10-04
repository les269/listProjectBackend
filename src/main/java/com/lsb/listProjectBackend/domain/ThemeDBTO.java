package com.lsb.listProjectBackend.domain;

import com.lsb.listProjectBackend.utils.Global;
import lombok.Data;

@Data
public class ThemeDBTO {
    private Global.ThemeDBType type;
    private String source;
    private String label;
    private String groups;

}
