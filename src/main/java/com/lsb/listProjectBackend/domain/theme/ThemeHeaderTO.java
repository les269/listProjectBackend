package com.lsb.listProjectBackend.domain.theme;

import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeCustom;
import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeDataset;
import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeImage;
import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeLabel;
import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeOtherSetting;
import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeTag;
import com.lsb.listProjectBackend.utils.Global;
import lombok.Data;

import java.util.List;

@Data
public class ThemeHeaderTO {
    private String name;
    private String version;
    private String title;
    private Global.ThemeHeaderType type;
    private ThemeImage themeImage;
    private List<ThemeLabel> themeLabelList;
    private List<ThemeDataset> themeDatasetList;
    private List<ThemeCustom> themeCustomList;
    private List<ThemeTag> themeTagList;
    private ThemeOtherSetting themeOtherSetting;
    private Integer seq;
}
