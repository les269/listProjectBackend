package com.lsb.listProjectBackend.entity.dynamic;

import com.lsb.listProjectBackend.utils.Global;
import lombok.Data;

import java.util.List;

@Data
public class ThemeOtherSetting {
    private List<String> rowColor;
    private Integer listPageSize;
    private boolean showDuplicate;
    private List<ThemeTopCustom> themeTopCustomList;
    private String checkFileExist;
    private boolean themeVisible;
    private boolean useQuickRefresh;
    private String quickRefresh;
    private Global.QuickRefreshType quickRefreshType;
    private String useSpider;
}
