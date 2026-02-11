package com.lsb.listProjectBackend.domain;

import lombok.Data;

@Data
public class ConnectionTestResultTO {
    private boolean success;
    private String message;

    public ConnectionTestResultTO() {}

    public ConnectionTestResultTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}