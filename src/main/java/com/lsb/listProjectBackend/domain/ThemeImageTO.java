package com.lsb.listProjectBackend.domain;


import com.lsb.listProjectBackend.utils.Global;
import lombok.Data;

@Data
public class ThemeImageTO {
    private Global.ThemeImageType type;
    private String imageKey;
    private String imageUrl;

}
