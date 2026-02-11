package com.lsb.listProjectBackend.entity.dynamic;

import com.lsb.listProjectBackend.converter.ScrapyPaginationConfigConverter;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "scrapy_pagination")
public class ScrapyPagination implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String name;

    @Convert(converter = ScrapyPaginationConfigConverter.class)
    private ScrapyPaginationConfig config;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedTime;
}
