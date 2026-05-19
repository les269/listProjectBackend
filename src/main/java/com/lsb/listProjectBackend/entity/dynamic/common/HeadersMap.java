package com.lsb.listProjectBackend.entity.dynamic.common;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import com.lsb.listProjectBackend.utils.Global;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "headers_map")
@IdClass(HeadersMapPK.class)
public class HeadersMap implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ref_id", nullable = false)
    private String refId;

    @Id
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private Global.HeadersMapType type;

    @Column(name = "headers_id", nullable = false)
    private String headersId;

    @UpdateTimestamp
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;
}
