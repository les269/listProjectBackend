package com.lsb.listProjectBackend.entity.dynamic.theme;

import com.lsb.listProjectBackend.utils.Global;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @Deprecated
    @Column(name = "theme_image")
    @JdbcTypeCode(SqlTypes.JSON)
    private ThemeImage themeImage;
    @Deprecated
    @Column(name = "theme_label_list")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<ThemeLabel> themeLabelList;
    @Deprecated
    @Column(name = "theme_dataset_list")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<ThemeDataset> themeDatasetList;
    @Deprecated
    @Column(name = "theme_custom_list")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<ThemeCustom> themeCustomList;
    @Deprecated
    @Column(name = "theme_tag_list")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<ThemeTag> themeTagList;
    @Deprecated
    @Column(name = "theme_other_setting")
    @JdbcTypeCode(SqlTypes.JSON)
    private ThemeOtherSetting themeOtherSetting;
    private Integer seq;

    @Serial
    private static final long serialVersionUID = 1L;

    public String getId() {
        return "ThemeHeader:" + name + "," + version + "," + type;
    }
}
