package com.lsb.listProjectBackend.entity;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ThemeDBPK implements Serializable {
    private Integer id;
    private String headerId;
    @Serial
    private static final long serialVersionUID = 1L;
}
