package com.lsb.listProjectBackend.service.impl;

import com.lsb.listProjectBackend.config.DatabaseInitializer;
import com.lsb.listProjectBackend.config.DynamicDataSourceRefresher;
import com.lsb.listProjectBackend.domain.DatabaseConfigTO;
import com.lsb.listProjectBackend.domain.SettingTO;
import com.lsb.listProjectBackend.entity.local.Setting;
import com.lsb.listProjectBackend.mapper.SettingMapper;
import com.lsb.listProjectBackend.repository.local.SettingRepository;
import com.lsb.listProjectBackend.service.DatabaseConfigService;
import com.lsb.listProjectBackend.service.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * local不需要 @UseDynamic or @UseDynamicTx
 */
@Service
public class SettingServiceImpl implements SettingService {

    @Autowired
    private SettingRepository settingRepository;
    @Autowired
    private DatabaseConfigService databaseConfigService;
    @Autowired
    private DynamicDataSourceRefresher dynamicDataSourceRefresher;

    private final SettingMapper settingMapper = SettingMapper.INSTANCE;

    @Override
    public List<SettingTO> getAll() {
        return settingMapper.toDomainList(settingRepository.findAll());
    }

    @Override
    public void update(SettingTO to) {
        settingRepository.save(settingMapper.toEntity(to));
    }

    @Override
    public void updateAll(List<SettingTO> to) {
        settingRepository.saveAll(settingMapper.toEntityList(to));
    }

    @Override
    public SettingTO getByName(String name) {
        return settingMapper.toDomain(settingRepository.findById(name).orElse(null));
    }

    @Override
    public void changeDatabase(SettingTO to) {
        dynamicDataSourceRefresher.refresh();

        DatabaseConfigTO databaseConfigTO = databaseConfigService.getById(to.getValue());
        if (databaseConfigTO != null) {
            // 根據資料庫類型初始化動態資料庫並執行 dynamic.sql
            DatabaseInitializer.initializeDynamicDatabase(databaseConfigTO);
        }

        Setting setting = settingMapper.toEntity(to);
        settingRepository.save(setting);
    }
}
