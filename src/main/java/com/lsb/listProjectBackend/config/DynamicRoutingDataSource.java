package com.lsb.listProjectBackend.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicRoutingDataSource extends AbstractRoutingDataSource {
    private final Map<Object, Object> dynamicTargetDataSources = new ConcurrentHashMap<>();

    @Override
    protected Object determineCurrentLookupKey() {
        return DynamicDataSourceContextHolder.getDataSourceKey();
    }

    /**
     * Register or replace a target DataSource at runtime.
     * <p>
     * NOTE: This re-initializes the underlying resolved datasource cache.
     */
    public void putDataSource(String key, Object dataSource) {
        dynamicTargetDataSources.put(key, dataSource);
        super.setTargetDataSources(dynamicTargetDataSources);
        super.afterPropertiesSet();
    }

    /**
     * Initialize all target datasources at once.
     */
    public void setAllDataSources(Map<Object, Object> dataSources, Object defaultDataSource) {
        dynamicTargetDataSources.clear();
        dynamicTargetDataSources.putAll(dataSources);
        super.setTargetDataSources(dynamicTargetDataSources);
        super.setDefaultTargetDataSource(defaultDataSource);
        super.afterPropertiesSet();
    }
}
