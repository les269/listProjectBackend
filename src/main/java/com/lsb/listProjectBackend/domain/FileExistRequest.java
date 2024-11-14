package com.lsb.listProjectBackend.domain;

import lombok.Data;

@Data
public class FileExistRequest {
    private String path;
    private String name;
}
