package com.lsb.listProjectBackend.entity.dynamic.common;

import java.util.List;

import com.lsb.listProjectBackend.entity.dynamic.spider.ValuePipeline;

import lombok.Data;

@Data
public class Header {
    private Integer seq;
    private String name;
    private String value;
    private List<ValuePipeline> valuePipelines;
}
