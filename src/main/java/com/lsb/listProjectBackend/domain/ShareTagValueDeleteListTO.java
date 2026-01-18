package com.lsb.listProjectBackend.domain;

import lombok.Data;

import java.util.List;

@Data
public class ShareTagValueDeleteListTO {
    private String shareTagId;
    private List<String> values;
}
