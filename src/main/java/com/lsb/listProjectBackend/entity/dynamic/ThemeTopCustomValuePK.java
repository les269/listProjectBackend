package com.lsb.listProjectBackend.entity.dynamic;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ThemeTopCustomValuePK implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String headerId;
    private String byKey;
}
