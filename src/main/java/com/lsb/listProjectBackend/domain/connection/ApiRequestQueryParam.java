package com.lsb.listProjectBackend.domain.connection;

import lombok.Data;
import java.io.Serializable;

@Data
public class ApiRequestQueryParam implements Serializable {
    private int seq;
    private String name;
    private String value;
    private boolean enabled;
}
