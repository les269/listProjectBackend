package com.lsb.listProjectBackend.domain;

import com.lsb.listProjectBackend.entity.dynamic.ThemeCustom;
import com.lsb.listProjectBackend.entity.dynamic.ThemeDataset;
import com.lsb.listProjectBackend.entity.dynamic.ThemeImage;
import com.lsb.listProjectBackend.entity.dynamic.ThemeLabel;
import com.lsb.listProjectBackend.entity.dynamic.ThemeOtherSetting;
import com.lsb.listProjectBackend.entity.dynamic.ThemeTag;
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
