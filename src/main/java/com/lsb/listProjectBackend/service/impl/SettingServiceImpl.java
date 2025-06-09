package com.lsb.listProjectBackend.service.impl;

import com.lsb.listProjectBackend.domain.SettingTO;
import com.lsb.listProjectBackend.mapper.SettingMapper;
import com.lsb.listProjectBackend.repository.SettingRepository;
import com.lsb.listProjectBackend.service.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SettingServiceImpl implements SettingService {

    @Autowired
    private SettingRepository settingRepository;

    private final SettingMapper settingMapper = SettingMapper.INSTANCE;

    @Override
    public List<SettingTO> getAll() {
        return settingMapper.toDomainList(settingRepository.findAll());
    }

    @Override
    public void update(SettingTO settingTO) {
        settingRepository.save(settingMapper.toEntity(settingTO));
    }

    @Override
    public void updateAll(List<SettingTO> settingTO) {
        settingRepository.saveAll(settingMapper.toEntityList(settingTO));
    }
}
