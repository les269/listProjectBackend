package com.lsb.listProjectBackend.entity;

import com.lsb.listProjectBackend.converter.ObjectListConverter;
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
@Table(name = "dataset_data")
public class DatasetData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "dataset_config_name")
    private String datasetConfigName;

    @Convert(converter = ObjectListConverter.class)
    private List<Object> data;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedTime;
}
