package com.lsb.listProjectBackend.entity.dynamic.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.lsb.listProjectBackend.utils.Global;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CookieListMapPK implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String refId;
    private Global.CookieListMapType type;
}
