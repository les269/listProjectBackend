package com.lsb.listProjectBackend.entity.dynamic.theme;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThemeCustomValuePK implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String headerId;
    private String byKey;
    private String correspondDataValue;
}
