package com.lsb.listProjectBackend.entity.dynamic;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "share_tag_value")
@IdClass(ShareTagValuePK.class)
public class ShareTagValue implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "share_tag_id")
    private String shareTagId;
    @Id
    @Column(name = "value")
    private String value;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedTime;
}
