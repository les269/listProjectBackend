package com.lsb.listProjectBackend.entity.dynamic.spider;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;

import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "spider_mapping")
@IdClass(SpiderMappingPK.class)
public class SpiderMapping implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "spider_id", nullable = false)
    private String spiderId;

    @Id
    @Column(name = "execution_order", nullable = false)
    private int executionOrder;

    @Column(name = "spider_item_id", nullable = false)
    private String spiderItemId;

    @UpdateTimestamp
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;
}