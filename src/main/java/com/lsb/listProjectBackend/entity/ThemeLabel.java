package com.lsb.listProjectBackend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.lsb.listProjectBackend.utils.Global.ThemeLabelType;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Entity
@Table(name = "theme_label")
@IdClass(ThemeLabelPK.class)
public class ThemeLabel implements Serializable {
    @Id
    private Integer id;
    @Id
    @Column(name = "header_id")
    private String headerId;
    @Enumerated(EnumType.ORDINAL)
    private ThemeLabelType type;
    private String seq;
    private String byKey;
    private String label;
    private String splitBy;
    private String useSpace;
    private boolean isSearch;
    private boolean isCopy;
    private boolean isVisible;
    private boolean isSort;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "header_id")
    private ThemeHeader themeHeader;

    @Serial
    private static final long serialVersionUID = 1L;
}
