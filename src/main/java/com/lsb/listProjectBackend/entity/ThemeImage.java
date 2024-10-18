package com.lsb.listProjectBackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lsb.listProjectBackend.utils.Global;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


@Data
public class ThemeImage  {
    @Enumerated(EnumType.STRING)
    private Global.ThemeImageType type;
    private String imageKey;
    private String imageUrl;
}
