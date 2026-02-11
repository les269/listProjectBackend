package com.lsb.listProjectBackend.entity.dynamic;

import com.lsb.listProjectBackend.converter.MapObjectConverter;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Entity
@Table(name = "replace_value_map")
public class ReplaceValueMap implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String name;

    @Convert(converter = MapObjectConverter.class)
    private Map<String, Object> map;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedTime;
}
