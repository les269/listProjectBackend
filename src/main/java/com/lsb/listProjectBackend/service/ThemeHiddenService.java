package com.lsb.listProjectBackend.service;

import com.lsb.listProjectBackend.domain.ThemeHiddenTO;

import java.util.List;

public interface ThemeHiddenService {
    List<ThemeHiddenTO> getAll();

    void save(ThemeHiddenTO req);

    void delete(String headerId);
}
