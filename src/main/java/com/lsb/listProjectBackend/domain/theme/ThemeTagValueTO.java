package com.lsb.listProjectBackend.domain.theme;

import java.util.List;

public record ThemeTagValueTO(String headerId, String tag, List<String> valueList) {
}
