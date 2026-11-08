package com.lsb.listProjectBackend.entity.dynamic.spider;

import com.lsb.listProjectBackend.utils.Global;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import java.util.List;

@Data
public class ExtractObjectsConfig {
    @Enumerated(EnumType.STRING)
    private Global.ExtractionRuleMode mode;
    private List<ExtractObjectField> fields;
}
