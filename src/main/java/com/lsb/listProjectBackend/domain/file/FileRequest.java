package com.lsb.listProjectBackend.domain.file;

import lombok.Data;

@Data
public class FileRequest {
    public String path;
    public String moveTo;
}
