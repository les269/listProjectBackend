package com.lsb.listProjectBackend.domain;

import com.lsb.listProjectBackend.entity.ScrapyPaginationConfig;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class ScrapyPaginationTO {
    private String name;
    private ScrapyPaginationConfig config;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
