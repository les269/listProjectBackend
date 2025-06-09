package com.lsb.listProjectBackend.service;

import com.lsb.listProjectBackend.domain.SettingTO;

import java.util.List;

public interface SettingService {
    List<SettingTO> getAll();
    void update(SettingTO settingTO);
    void updateAll(List<SettingTO> settingTO);
}
