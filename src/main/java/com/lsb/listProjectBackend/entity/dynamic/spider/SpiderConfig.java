package com.lsb.listProjectBackend.entity.dynamic.spider;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "spider_config")
public class SpiderConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "spider_id", nullable = false)
    private String spiderId;

    @Column(nullable = false)
    private String description;

    @Column(name = "prime_key_size", nullable = false)
    private Integer primeKeySize;

    @Column(name = "test_data", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private SpiderConfigTestData testData;

    @Column(name = "is_url_based", nullable = false)
    private Boolean isUrlBased;

    @UpdateTimestamp
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;
}