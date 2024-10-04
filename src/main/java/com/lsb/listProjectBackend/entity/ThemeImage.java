package com.lsb.listProjectBackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lsb.listProjectBackend.utils.Global;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


@Data
@Entity
@Table(name = "theme_image")
public class ThemeImage implements Serializable {
    @Id
    private String headerId;
    @Enumerated(EnumType.ORDINAL)
    private Global.ThemeImageType type;
    private String imageKey;
    private String imageUrl;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "header_id")
    private ThemeHeader themeHeader;

    @Serial
    private static final long serialVersionUID = 1L;
}
