package com.lsb.listProjectBackend.entity.dynamic;

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
@Table(name = "theme_item")
@IdClass(ThemeItemPK.class)
public class ThemeItem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "item_id", nullable = false)
    private String itemId;

    @Id
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ThemeItemType type;

    @Column(name = "json", nullable = false)
    private String json;

    @Column(name = "description")
    private String description;

    @UpdateTimestamp
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;
}
