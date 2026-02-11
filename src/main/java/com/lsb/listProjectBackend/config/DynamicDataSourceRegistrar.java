package com.lsb.listProjectBackend.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 啟動時呼叫 {@link DynamicDataSourceRefresher#refresh()}，依 local DB 的
 * {@code database_config} 註冊 dynamic 資料庫 targets。
 */
@Component
public class DynamicDataSourceRegistrar implements ApplicationListener<ApplicationReadyEvent> {

    private final DynamicDataSourceRefresher refresher;

    public DynamicDataSourceRegistrar(DynamicDataSourceRefresher refresher) {
        this.refresher = refresher;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        refresher.refresh();
    }
}
