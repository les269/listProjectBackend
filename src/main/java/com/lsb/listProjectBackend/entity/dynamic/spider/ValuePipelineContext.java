package com.lsb.listProjectBackend.entity.dynamic.spider;

import com.jayway.jsonpath.DocumentContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jsoup.select.Elements;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValuePipelineContext {
    private DocumentContext result;// 當前所有資料的就是一個json物件 可能是初始的空json 也可能是經過前面pipeline處理後的json物件
    private DocumentContext extraContext;// 額外的上下文資料 如cookies headers等 在下一項目可以使用已經爬出的結果
    private Elements elements;
    private Map<String, String> cookies;
    private Map<String, String> headers;
}
