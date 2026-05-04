package com.lsb.listProjectBackend.domain.dataset;

import com.lsb.listProjectBackend.utils.Global;

import java.util.List;

public record DatasetQuickRefreshTO(
        String byKey,
        String primeKey,
        String scrapyName,
        String datasetName,
        String url,
        Global.QuickRefreshType quickRefreshType,
        List<String> params) {
}
