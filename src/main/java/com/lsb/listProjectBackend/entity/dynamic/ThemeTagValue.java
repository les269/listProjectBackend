package com.lsb.listProjectBackend.entity.dynamic;

import com.lsb.listProjectBackend.converter.StringListConverter;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "theme_tag_value")
@IdClass(ThemeTagValuePK.class)
public class ThemeTagValue {
    @Id
    @Column(name = "header_id")
    private String headerId;
    @Id
    private String tag;

    @Column(name = "value_list")
    @Convert(converter = StringListConverter.class)
    private List<String> valueList;
}
