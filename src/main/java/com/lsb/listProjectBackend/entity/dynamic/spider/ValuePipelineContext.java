package com.lsb.listProjectBackend.entity.dynamic.spider;

import com.jayway.jsonpath.DocumentContext;
import com.lsb.listProjectBackend.domain.common.ReplaceValueMapTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jsoup.select.Elements;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValuePipelineContext {
    private List<ReplaceValueMapTO> replaceValueMapList;
    private DocumentContext result;
    private Elements elements;
}
