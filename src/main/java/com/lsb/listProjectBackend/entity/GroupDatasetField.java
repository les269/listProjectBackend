package com.lsb.listProjectBackend.entity;

import com.lsb.listProjectBackend.utils.Global;
import lombok.Data;

@Data
public class GroupDatasetField {
    private Integer seq;
    private Global.GroupDatasetFieldType type;
    private String key;
    private String label;
    private String fixedString;
}
