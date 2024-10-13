package com.lsb.listProjectBackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.lsb.listProjectBackend.utils.Global;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Entity
@Table(name = "theme_header")
public class ThemeHeader implements Serializable {

    @Id
    @Column(name = "header_id")
    private String headerId;
    private String name;
    private String version;
    @Enumerated(EnumType.STRING)
    private Global.ThemeHeaderType type;
    private String title;
    private Long updateTime;
    @OneToOne(mappedBy = "themeHeader", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ThemeImage themeImage;
    @OneToMany(mappedBy = "themeHeader", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ThemeLabel> themeLabelList;
    @OneToMany(mappedBy = "themeHeader", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ThemeDB> themeDBList;
    @OneToMany(mappedBy = "themeHeader", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ThemeCustom> themeCustomList;

    @Serial
    private static final long serialVersionUID = 1L;

    public String getId() {
        return "ThemeHeader:" + name + "," + version + "," + type;
    }
}
