package com.lsb.listProjectBackend.entity;

import com.lsb.listProjectBackend.utils.Global;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Entity
@Table(name = "api_config")
public class ApiConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "api_name")
    private String apiName;

    @Column(name = "http_method")
    @Enumerated(EnumType.STRING)
    private Global.HttpMethodType httpMethod;
    @Column(name = "endpoint_url")
    private String endpointUrl;
    @Column(name = "request_body")
    private String requestBody;
    @Column(name = "http_params")
    private String httpParams;
    @Column(name = "http_headers")
    private String httpHeaders;
    @Column(name = "success_message")
    private String successMessage;
    @Column(name = "updated_time")
    private Long updatedTime;
}
