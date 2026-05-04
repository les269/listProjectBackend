package com.lsb.listProjectBackend.domain.connection;

import com.lsb.listProjectBackend.utils.Global;

public record ApiConfigTO(
        String apiName,
        Global.HttpMethodType httpMethod,
        String endpointUrl,
        String requestBody,
        String httpParams,
        String httpHeaders,
        String successMessage,
        Long updatedTime) {
}
