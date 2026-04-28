package com.lsb.listProjectBackend.config.datasource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 支援執行期間動態切換的 DataSource。
 *
 * 繼承 Spring 的 {@link AbstractRoutingDataSource}，其核心機制是：
 * JPA 每次需要資料庫連線前，會呼叫 {@link #determineCurrentLookupKey()} 取得一個 key，
 * 再從已註冊的 targetDataSources Map 中找出對應的 DataSource 物件。
 *
 * 本類別將 key 的來源委派給 {@link DynamicDataSourceContextHolder}（ThreadLocal），
 * 因此同一 JVM 內不同 thread（request）可以各自連接不同的資料庫，互不干擾。
 */
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

    /**
     * 自行維護的 target 清單，供 {@link #putDataSource} 和 {@link #setAllDataSources} 使用。
     * 使用 ConcurrentHashMap 保證多執行緒修改時的安全性。
     */
    private final Map<Object, Object> dynamicTargetDataSources = new ConcurrentHashMap<>();

    /**
     * Spring JPA 每次執行 SQL 前都會呼叫此方法，取得「要用哪個 DataSource」的 key。
     * key 由 {@link DynamicDataSourceContextHolder} 從 ThreadLocal 讀取，
     * 也就是 {@link DynamicDatabaseAspect} 在方法進入前設定的值。
     *
     * @return 目前 thread 對應的 DB key；若為 null，Spring 會使用 defaultTargetDataSource。
     */
    @Override
    protected Object determineCurrentLookupKey() {
        return DynamicDataSourceContextHolder.getDataSourceKey();
    }

    /**
     * 執行期間新增或更換單一 DataSource target。
     * 呼叫後會重新初始化父類別的 resolved cache（afterPropertiesSet），
     * 下一次 JPA 操作即可使用新的 DataSource。
     *
     * @param key        routing key，對應 database_config.config_name
     * @param dataSource 要註冊的 DataSource 物件
     */
    public void putDataSource(String key, Object dataSource) {
        dynamicTargetDataSources.put(key, dataSource);
        super.setTargetDataSources(dynamicTargetDataSources);
        super.afterPropertiesSet();
    }

    /**
     * 一次性替換全部 DataSource targets（啟動時與 refresh 時使用）。
     * 會清除舊的所有 targets，再填入新的，並重新初始化 resolved cache。
     *
     * @param dataSources       新的 key → DataSource 對應表
     * @param defaultDataSource 找不到 key 時的 fallback DataSource
     */
    public void setAllDataSources(Map<Object, Object> dataSources, Object defaultDataSource) {
        dynamicTargetDataSources.clear();
        dynamicTargetDataSources.putAll(dataSources);
        super.setTargetDataSources(dynamicTargetDataSources);
        super.setDefaultTargetDataSource(defaultDataSource);
        super.afterPropertiesSet();
    }
}
