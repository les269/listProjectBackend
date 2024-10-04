package com.lsb.listProjectBackend.repository;

import com.lsb.listProjectBackend.entity.ThemeLabel;
import com.lsb.listProjectBackend.entity.ThemeLabelPK;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThemeLabelRepository extends JpaRepository<ThemeLabel, ThemeLabelPK> {
}
