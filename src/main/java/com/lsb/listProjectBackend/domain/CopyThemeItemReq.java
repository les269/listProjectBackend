package com.lsb.listProjectBackend.domain;

import com.lsb.listProjectBackend.utils.Global;
import lombok.Data;

@Data
public class CopyThemeItemReq {
    private String sourceItemId;
    private String targetItemId;
    private Global.ThemeItemType type;
}