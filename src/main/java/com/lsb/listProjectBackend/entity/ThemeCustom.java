package com.lsb.listProjectBackend.entity;

import com.lsb.listProjectBackend.utils.Global;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Entity
@Table(name = "theme_custom")
@IdClass(ThemeCustomPK.class)
public class ThemeCustom implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;
    @Id
    @Column(name = "header_id")
    private String headerId;
    @Enumerated(EnumType.STRING)
    private Global.ThemeCustomType type;
    @Id
    @Column(name = "by_key")
    private String byKey;

    private String label;
    private String seq;
    @Column(name = "open_url")
    private String openUrl;
    @Column(name = "open_url_by_key")
    private String openUrlByKey;
    @Column(name = "copy_value")
    private String copyValue;
    @Column(name = "copy_value_by_key")
    private String copyValueByKey;
    @Column(name = "button_icon_fill")
    private String buttonIconFill;
    @Column(name = "button_icon_fill_color")
    private String buttonIconFillColor;
    @Column(name = "button_icon_true")
    private String buttonIconTrue;
    @Column(name = "button_icon_false")
    private String buttonIconFalse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "header_id")
    private ThemeHeader themeHeader;

}
