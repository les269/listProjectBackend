package com.lsb.listProjectBackend.domain.connection;

import tools.jackson.databind.JsonNode;

public record SingalCallApiReqTO(String apiId, JsonNode json) {
    public SingalCallApiReqTO {
        if (apiId == null || apiId.isBlank()) {
            throw new IllegalArgumentException("apiId is required");
        }
        if (json == null) {
            throw new IllegalArgumentException("json is required");
        }
    }
}
