package com.lsb.listProjectBackend.domain.spider;

import lombok.Data;

import java.util.List;

@Data
public class SpiderReqTO {
    private String spiderId;
    private String url;
    private List<String> primeKeyList;
}