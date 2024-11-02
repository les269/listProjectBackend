package com.lsb.listProjectBackend.domain;

import com.lsb.listProjectBackend.entity.ScrapyData;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ScrapyTestTO {
    private ScrapyData scrapyData;
    private List<ScrapyData> scrapyDataList;
    private List<String> json;
    private String url;
}
