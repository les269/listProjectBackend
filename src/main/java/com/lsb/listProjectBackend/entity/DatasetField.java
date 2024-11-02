package com.lsb.listProjectBackend.entity;

import com.lsb.listProjectBackend.utils.Global;
import lombok.Data;

@Data
public class DatasetField {
    private Integer seq;
    private Global.DatasetFieldType type;
    private String key;
    private String label;
}
