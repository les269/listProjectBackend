package com.lsb.listProjectBackend.service;

import com.lsb.listProjectBackend.domain.ScrapyPaginationTO;
import com.lsb.listProjectBackend.domain.ScrapyPaginationTestTO;

import java.util.List;
import java.util.Map;

public interface ScrapyPaginationService {

    ScrapyPaginationTO get(String name);
    void update(ScrapyPaginationTO req);
    void delete(String name);
    boolean exist(String name);
    List<ScrapyPaginationTO> getAll();
    Map<String, Object> testHtml(ScrapyPaginationTestTO to);
    ScrapyPaginationTO updateRedirectData(String name);
    boolean checkForUpdate(String name);
}
