package com.lsb.listProjectBackend.domain.theme;

import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeCustom;
import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeDataset;
import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeImage;
import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeLabel;
import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeOtherSetting;
import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeTag;
import com.lsb.listProjectBackend.utils.Global;

import java.util.List;

public record ThemeHeaderTO(
        String headerId,
        String name,
        String version,
        String title,
        Global.ThemeHeaderType type,
        ThemeImage themeImage,
        List<ThemeLabel> themeLabelList,
        List<ThemeDataset> themeDatasetList,
        List<ThemeCustom> themeCustomList,
        List<ThemeTag> themeTagList,
        ThemeOtherSetting themeOtherSetting,
        Integer seq,
        Long updateTime) {
}
