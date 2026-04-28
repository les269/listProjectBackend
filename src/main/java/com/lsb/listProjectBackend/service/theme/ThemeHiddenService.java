package com.lsb.listProjectBackend.service.theme;

import com.lsb.listProjectBackend.domain.theme.ThemeHiddenTO;

import java.util.List;

public interface ThemeHiddenService {
    List<ThemeHiddenTO> getAll();

    void save(ThemeHiddenTO req);

    void delete(String headerId);
}
