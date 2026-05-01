package com.lsb.listProjectBackend.entity.dynamic.dataset;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Entity
@Table(name = "group_dataset_data")
@IdClass(GroupDatasetDataPK.class)
@AllArgsConstructor
@NoArgsConstructor
public class GroupDatasetData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "group_name")
    private String groupName;
    @Id
    @Column(name = "prime_value")
    private String primeValue;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> json;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedTime;

}
