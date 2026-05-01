package com.lsb.listProjectBackend.entity.dynamic.spider;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "spider_item")
public class SpiderItem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "spider_item_id", nullable = false)
    private String spiderItemId;

    @Column(nullable = false)
    private String description;

    @Column(name = "item_setting", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private SpiderItemSetting itemSetting;

    @UpdateTimestamp
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;
}
