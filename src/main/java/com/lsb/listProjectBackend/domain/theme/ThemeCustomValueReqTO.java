package com.lsb.listProjectBackend.domain.theme;

import java.util.List;

public record ThemeCustomValueReqTO(String headerId, List<String> valueList) {
}
