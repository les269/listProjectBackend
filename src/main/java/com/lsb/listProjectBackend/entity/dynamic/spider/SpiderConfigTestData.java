package com.lsb.listProjectBackend.entity.dynamic.spider;

import lombok.Data;

import java.util.List;

@Data
public class SpiderConfigTestData {
    private List<String> primeKeyList;
    private String url;
    private String resultJson;
}