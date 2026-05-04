package com.lsb.listProjectBackend.domain.scrapy;

import java.util.List;

public record ScrapyReqTO(String scrapyName, List<String> json, String url) {
}
