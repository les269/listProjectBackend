package com.lsb.listProjectBackend.domain.theme;

import com.lsb.listProjectBackend.utils.Global;

public record CopyThemeItemReqTO(String sourceItemId, String targetItemId, Global.ThemeItemType type) {
}