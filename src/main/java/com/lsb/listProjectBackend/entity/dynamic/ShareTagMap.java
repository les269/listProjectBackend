package com.lsb.listProjectBackend.entity.dynamic;

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
@Table(name = "share_tag_map")
@IdClass(ShareTagMapPK.class)
public class ShareTagMap implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "share_tag_id", nullable = false)
    private String shareTagId;

    @Id
    @Column(name = "theme_header_id", nullable = false)
    private String themeHeaderId;

    @UpdateTimestamp
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;
}
