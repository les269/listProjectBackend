package com.lsb.listProjectBackend.entity.dynamic.spider;

import java.util.List;

import lombok.Data;

@Data
public class CalculateConfig {
    private String expression;
    private String defaultValue;
    private List<ChipsMapValue> variablePaths;
}
