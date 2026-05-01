package com.lsb.listProjectBackend.entity.dynamic.spider;

import com.lsb.listProjectBackend.utils.Global;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class InsertConfig {
    @Enumerated(EnumType.STRING)
    private Global.PositionType position;
    private String key;
    private int index;
    private String text;
}
