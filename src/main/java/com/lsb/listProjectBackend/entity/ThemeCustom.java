package com.lsb.listProjectBackend.entity;

import com.lsb.listProjectBackend.utils.Global;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
public class ThemeCustom  {

    @Enumerated(EnumType.STRING)
    private Global.ThemeCustomType type;
    private String byKey;

    private String label;
    private String seq;
    private String openUrl;
    private String copyValue;
    private String buttonIconFill;
    private String buttonIconFillColor;
    private String buttonIconTrue;
    private String buttonIconFalse;
    private ApiConfig apiConfig;
    private String deleteFile;
    private String filePathForMoveTo;
    private String moveTo;
    private Global.OpenWindowTargetType openWindowsTarget;
    private String openFolder;
    private List<String> visibleDatasetNameList;

}
