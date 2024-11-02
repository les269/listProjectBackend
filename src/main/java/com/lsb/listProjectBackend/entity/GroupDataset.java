package com.lsb.listProjectBackend.entity;

import com.lsb.listProjectBackend.converter.ObjectConverter;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "group_dataset")
@IdClass(GroupDatasetPK.class)
public class GroupDataset  implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "group_name")
    private String groupName;
    @Id
    @Column(name = "prime_value")
    private String primeValue;

    @Convert(converter = ObjectConverter.class)
    private Object json;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedTime;

}
