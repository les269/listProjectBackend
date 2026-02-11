package com.lsb.listProjectBackend.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import com.lsb.listProjectBackend.entity.dynamic.ScrapyData;

@Data
public class ScrapyConfigTO {
    private String name;
    private Integer paramSize;
    private List<ScrapyData> data;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private String testJson;
    private String testUrl;
}
