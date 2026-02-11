package com.lsb.listProjectBackend.service;

import com.lsb.listProjectBackend.domain.SettingTO;

import java.util.List;

public interface SettingService {
    List<SettingTO> getAll();
    void update(SettingTO to);
    void updateAll(List<SettingTO> to);
    SettingTO getByName(String name);
    void changeDatabase(SettingTO to);
}
