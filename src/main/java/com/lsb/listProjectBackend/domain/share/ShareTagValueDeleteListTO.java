package com.lsb.listProjectBackend.domain.share;

import lombok.Data;

import java.util.List;

@Data
public class ShareTagValueDeleteListTO {
    private String shareTagId;
    private List<String> values;
}
