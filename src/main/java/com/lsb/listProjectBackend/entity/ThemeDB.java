package com.lsb.listProjectBackend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lsb.listProjectBackend.utils.Global;
import jakarta.persistence.*;
import lombok.Data;


@Data
public class ThemeDB {
    @Enumerated(EnumType.STRING)
    private Global.ThemeDBType type;
    private String source;
    private String label;
    private String groups;
    private String seq;
    @JsonProperty("isDefault")
    private boolean isDefault;

}
