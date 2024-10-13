package com.lsb.listProjectBackend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Entity
@Table(name = "theme_custom_value")
@IdClass(ThemeCustomValuePK.class)
public class ThemeCustomValue implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name="header_id")
    private String headerId;
    @Id
    @Column(name="by_key")
    private String byKey;
    @Id
    @Column(name="correspond_data_value")
    private String correspondDataValue;

    @Column(name="custom_value")
    private String customValue;

}
