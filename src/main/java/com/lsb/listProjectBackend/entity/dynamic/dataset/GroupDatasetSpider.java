package com.lsb.listProjectBackend.entity.dynamic.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GroupDatasetSpider {
    private Integer seq;
    private String spiderId;
    private String label;
    private boolean visibleJson;
    private boolean visibleUrl;
    @JsonProperty("isDefault")
    private boolean isDefault;
}
