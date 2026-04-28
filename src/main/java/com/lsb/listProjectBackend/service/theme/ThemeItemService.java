package com.lsb.listProjectBackend.service.theme;

import com.lsb.listProjectBackend.domain.theme.CopyThemeItemReq;
import com.lsb.listProjectBackend.domain.theme.ThemeItemMapTO;
import com.lsb.listProjectBackend.domain.theme.ThemeItemTO;
import com.lsb.listProjectBackend.utils.Global;

import java.util.List;

public interface ThemeItemService {
    void copyThemeItem(CopyThemeItemReq req);

    ThemeItemTO getThemeItemById(Global.ThemeItemType type, String itemId);

    void updateThemeItem(ThemeItemTO req);

    void deleteThemeItem(Global.ThemeItemType type, String itemId);

    List<ThemeItemTO> getAllThemeItem(Global.ThemeItemType type);

    List<ThemeItemMapTO> getAllThemeItemMapByType(Global.ThemeItemType type);

    List<ThemeItemTO> getItemsByHeaderId(String headerId);

    void updateThemeItemMap(ThemeItemMapTO req);

    void deleteThemeItemMap(Global.ThemeItemType type, String headerId);

    void deleteThemeItemMapByHeaderId(String headerId);

    boolean themeItemMapInUse(Global.ThemeItemType type, String itemId);

    void copyMapping(String sourceHeaderId, String targetHeaderId);
}
