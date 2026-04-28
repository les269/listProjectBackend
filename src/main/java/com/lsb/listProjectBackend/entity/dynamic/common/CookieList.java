package com.lsb.listProjectBackend.entity.dynamic.common;

import com.lsb.listProjectBackend.converter.common.CookieListConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "cookie_list")
public class CookieList implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "cookie_id", nullable = false)
    private String cookieId;

    @Column(name = "list", nullable = false)
    @Convert(converter = CookieListConverter.class)
    private List<Cookie> list;

    @Column(name = "description")
    private String description;

    @UpdateTimestamp
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;
}
