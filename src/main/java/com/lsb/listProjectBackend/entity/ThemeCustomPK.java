package com.lsb.listProjectBackend.entity;

import com.lsb.listProjectBackend.utils.Global;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ThemeCustomPK implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String headerId;
    @Enumerated(EnumType.STRING)
    private Global.ThemeCustomType type;
    private String byKey;
}
