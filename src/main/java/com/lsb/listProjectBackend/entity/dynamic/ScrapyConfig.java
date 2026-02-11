package com.lsb.listProjectBackend.entity.dynamic;

import com.lsb.listProjectBackend.converter.ScrapyDataListConverter;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "scrapy_config")
public class ScrapyConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String name;
    @Column(name = "param_size")
    private Integer paramSize;

    @Column(nullable = false)
    @Convert(converter = ScrapyDataListConverter.class)
    private List<ScrapyData> data;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedTime;

    @Column(name = "test_json")
    private String testJson;
    @Column(name = "test_url")
    private String testUrl;
}
