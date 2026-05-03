package com.lsb.listProjectBackend.entity.dynamic.spider;

import com.lsb.listProjectBackend.utils.Global;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class ChineseConvert {
    @Enumerated(EnumType.STRING)
    private Global.ChineseConvertType chineseConvertType;
    @Enumerated(EnumType.STRING)
    private Global.ZhConverterUtilType zhConverterUtilType;
}
