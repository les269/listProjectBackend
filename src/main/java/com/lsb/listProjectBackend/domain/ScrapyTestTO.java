package com.lsb.listProjectBackend.domain;

import lombok.Data;

import java.util.List;
import java.util.Map;

import com.lsb.listProjectBackend.entity.dynamic.ScrapyData;

@Data
public class ScrapyTestTO {
    private ScrapyData scrapyData;
    private List<ScrapyData> scrapyDataList;
    private List<String> json;
    private String url;
}
