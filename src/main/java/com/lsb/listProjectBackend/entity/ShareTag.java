package com.lsb.listProjectBackend.entity;

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
@Table(name = "share_tag")
public class ShareTag implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name="share_tag_id")
    private String shareTagId;
    @Column(name="share_tag_name")
    private String shareTagName;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedTime;
}
