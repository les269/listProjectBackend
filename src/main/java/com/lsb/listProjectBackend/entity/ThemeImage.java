package com.lsb.listProjectBackend.entity;

import com.lsb.listProjectBackend.utils.Global;
import jakarta.persistence.*;
import lombok.Data;


@Data
public class ThemeImage  {
    @Enumerated(EnumType.STRING)
    private Global.ThemeImageType type;
    private String imageKey;
    private String imageUrl;
}
