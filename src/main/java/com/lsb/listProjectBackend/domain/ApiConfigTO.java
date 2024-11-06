package com.lsb.listProjectBackend.domain;

import com.lsb.listProjectBackend.utils.Global;
import lombok.Data;

@Data
public class ApiConfigTO {
    private String apiName;
    private Global.HttpMethodType httpMethod;
    private String endpointUrl;
    private String requestBody;
    private String httpParams;
    private String httpHeaders;
    private String successMessage;
    private Long updatedTime;
}
