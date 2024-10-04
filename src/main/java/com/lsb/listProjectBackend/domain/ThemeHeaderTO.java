package com.lsb.listProjectBackend.domain;

import com.lsb.listProjectBackend.utils.Global;
import lombok.Data;

@Data
public class ThemeHeaderTO {
    private String name;
    private String version;
    private String title;
    private Global.ThemeHeaderType type;

}
