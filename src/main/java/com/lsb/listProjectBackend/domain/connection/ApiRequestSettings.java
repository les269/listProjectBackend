package com.lsb.listProjectBackend.domain.connection;

import com.lsb.listProjectBackend.utils.Global;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class ApiRequestSettings implements Serializable {
    private Global.HttpMethodType httpMethod;
    private String endpointUrl;
    private String requestBody;
    private List<ApiRequestQueryParam> httpParams;
    private Global.ApiRequestBodyType bodyType;
    private List<ApiRequestQueryParam> bodyFormFields;
    private Long timeoutMs;
    private Boolean enabled;
    private String successMessage;
}
