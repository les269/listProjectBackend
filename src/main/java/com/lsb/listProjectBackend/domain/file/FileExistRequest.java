package com.lsb.listProjectBackend.domain.file;

import lombok.Data;

@Data
public class FileExistRequest {
    private String path;
    private String name;
}
