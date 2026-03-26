package com.lsb.listProjectBackend.entity.dynamic;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

@Data
@Entity
@Table(name = "theme_hidden")
public class ThemeHidden implements Serializable {
    @Id
    @Column(name = "header_id")
    private String headerId;

    @UpdateTimestamp
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;
}
