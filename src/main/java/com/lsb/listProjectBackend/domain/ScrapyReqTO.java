package com.lsb.listProjectBackend.domain;

import lombok.Data;

import java.util.List;

@Data
public class ScrapyReqTO {
    private String scrapyName;
    private List<String> json;
    private String url;
}
