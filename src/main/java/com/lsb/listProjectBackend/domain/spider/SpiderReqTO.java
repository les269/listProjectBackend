package com.lsb.listProjectBackend.domain.spider;

import java.util.List;

public record SpiderReqTO(String spiderId, String url, List<String> primeKeyList) {
}
