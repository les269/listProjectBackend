package com.lsb.listProjectBackend.entity.local;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "disk")
public class Disk implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String disk;

    @Column(name = "total_space", nullable = false)
    private Long totalSpace = 0L;

    @Column(name = "free_space", nullable = false)
    private Long freeSpace = 0L;

    @Column(name = "usable_space", nullable = false)
    private Long usableSpace = 0L;

    @UpdateTimestamp
    @Column(name = "update_date", nullable = false)
    private LocalDateTime updateDate;
}
