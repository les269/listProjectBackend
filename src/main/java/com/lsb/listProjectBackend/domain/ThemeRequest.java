package com.lsb.listProjectBackend.domain;

import lombok.Data;

import java.util.List;

@Data
public class ThemeRequest {
    private ThemeHeaderTO themeHeader;
    private ThemeImageTO themeImage;
    private List<ThemeLabelTO> themeLabelList;
    private List<ThemeDBTO> themeDBList;
}
