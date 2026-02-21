package com.lsb.listProjectBackend.entity.dynamic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ThemeDataset {
    private List<String> datasetList;
    private String label;
    private int seq;
    @JsonProperty("isDefault")
    private boolean isDefault;

}
