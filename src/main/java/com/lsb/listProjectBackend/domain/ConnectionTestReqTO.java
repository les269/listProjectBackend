package com.lsb.listProjectBackend.domain;

import lombok.Data;

@Data
public class ConnectionTestReqTO {
    private String databaseType;
    private String sqliteFilePath;
    private String host;
    private Integer port;
    private String databaseName;
    private String username;
    private String password;
}
