package com.lsb.listProjectBackend.domain.connection;

import java.time.LocalDateTime;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public record ApiRequestConfigTO(
        String apiId,
        String description,
        ApiRequestSettings config,
        LocalDateTime createdTime,
        LocalDateTime updatedTime) {
}
