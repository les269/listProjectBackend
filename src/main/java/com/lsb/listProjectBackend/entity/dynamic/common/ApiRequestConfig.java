package com.lsb.listProjectBackend.entity.dynamic.common;

import com.lsb.listProjectBackend.domain.connection.ApiRequestSettings;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "api_request_config")
public class ApiRequestConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "api_id", nullable = false)
    private String apiId;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private ApiRequestSettings config;

    @CreationTimestamp
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;
}
