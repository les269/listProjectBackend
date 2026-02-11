package com.lsb.listProjectBackend.entity.dynamic;

import com.lsb.listProjectBackend.utils.Global;
import lombok.Data;

@Data
public class DatasetField {
    private Integer seq;
    private Global.DatasetFieldType type;
    private String key;
    private String label;
    private String fixedString;
    private String replaceRegular;
    private String replaceRegularTo;
}
