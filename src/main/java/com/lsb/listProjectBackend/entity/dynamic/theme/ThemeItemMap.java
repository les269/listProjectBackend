package com.lsb.listProjectBackend.entity.dynamic.theme;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import com.lsb.listProjectBackend.utils.Global.ThemeItemType;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "theme_item_map")
@IdClass(ThemeItemMapPK.class)
public class ThemeItemMap implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "item_id", nullable = false)
    private String itemId;

    @Id
    @Column(name = "header_id", nullable = false)
    private String headerId;

    @Id
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ThemeItemType type;

    @UpdateTimestamp
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;
}
