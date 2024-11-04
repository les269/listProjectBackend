package com.lsb.listProjectBackend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GroupDatasetScrapy {
    private Integer seq;
    private String name;
    private String label;
    private boolean visibleJson;
    private boolean visibleUrl;
    @JsonProperty("isDefault")
    private boolean isDefault;
}
