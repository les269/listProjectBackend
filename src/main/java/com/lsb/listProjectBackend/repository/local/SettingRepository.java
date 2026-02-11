package com.lsb.listProjectBackend.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import com.lsb.listProjectBackend.entity.local.Setting;

public interface SettingRepository extends JpaRepository<Setting, String> {

}
