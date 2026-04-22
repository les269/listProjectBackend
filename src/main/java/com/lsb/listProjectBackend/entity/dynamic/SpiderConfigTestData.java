package com.lsb.listProjectBackend.entity.dynamic;

import lombok.Data;

import java.util.List;

@Data
public class SpiderConfigTestData {
    private List<String> pkArray;
    private String url;
    private String resultJson;
}