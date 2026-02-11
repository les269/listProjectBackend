package com.lsb.listProjectBackend.entity.dynamic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShareTagMapPK implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String shareTagId;
    private String themeHeaderId;
}
