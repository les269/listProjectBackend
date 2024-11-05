package com.lsb.listProjectBackend.domain;

import com.lsb.listProjectBackend.entity.ThemeCustom;
import com.lsb.listProjectBackend.entity.ThemeDataset;
import com.lsb.listProjectBackend.entity.ThemeImage;
import com.lsb.listProjectBackend.entity.ThemeLabel;
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
}
