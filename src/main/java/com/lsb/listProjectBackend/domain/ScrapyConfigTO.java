package com.lsb.listProjectBackend.domain;

import com.lsb.listProjectBackend.entity.ScrapyData;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ScrapyConfigTO {
    private String name;
    private String paramSize;
    private List<ScrapyData> data;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private String testJson;
    private String testUrl;
}
