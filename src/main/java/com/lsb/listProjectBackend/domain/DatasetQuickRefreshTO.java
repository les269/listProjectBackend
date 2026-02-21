package com.lsb.listProjectBackend.domain;

import com.lsb.listProjectBackend.utils.Global;
import lombok.Data;

import java.util.List;

@Data
public class DatasetQuickRefreshTO {
    private String byKey;
    private String primeKey;
    private String scrapyName;
    private String datasetName;
    private String url;
    private Global.QuickRefreshType quickRefreshType;
    private List<String> params;
}
