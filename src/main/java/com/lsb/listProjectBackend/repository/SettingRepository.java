package com.lsb.listProjectBackend.repository;

import com.lsb.listProjectBackend.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingRepository extends JpaRepository<Setting, String> {

}
