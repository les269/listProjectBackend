package com.lsb.listProjectBackend.repository.dynamic.theme;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeHidden;

public interface ThemeHiddenRepository extends JpaRepository<ThemeHidden, String> {
}
