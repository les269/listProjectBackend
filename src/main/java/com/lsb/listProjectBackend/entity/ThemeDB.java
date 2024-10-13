package com.lsb.listProjectBackend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.lsb.listProjectBackend.utils.Global;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


@Data
@Entity
@Table(name = "theme_db")
@IdClass(ThemeDBPK.class)
public class ThemeDB implements Serializable {
    @Id
    private Integer id;
    @Id
    @Column(name = "header_id")
    private String headerId;
    @Enumerated(EnumType.STRING)
    private Global.ThemeDBType type;
    private String source;
    private String label;
    private String groups;
    private String seq;
    private boolean isDefault;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "header_id")
    private ThemeHeader themeHeader;

    @Serial
    private static final long serialVersionUID = 1L;
}
