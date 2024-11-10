package com.lsb.listProjectBackend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Entity
    @Table(name = "theme_top_custom_value")
@IdClass(ThemeTopCustomValuePK.class)
public class ThemeTopCustomValue implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name="header_id")
    private String headerId;
    @Id
    @Column(name="by_key")
    private String byKey;

    @Column(name="custom_value")
    private String customValue;
}
