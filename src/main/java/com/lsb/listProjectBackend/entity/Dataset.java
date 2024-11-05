package com.lsb.listProjectBackend.entity;

import com.lsb.listProjectBackend.converter.DatasetConfigConverter;
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
@Table(name = "dataset")
public class Dataset implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String name;

    @Convert(converter = DatasetConfigConverter.class)
    private DatasetConfig config;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedTime;
}
