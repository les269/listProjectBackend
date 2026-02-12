package com.lsb.listProjectBackend.service;

import com.lsb.listProjectBackend.domain.DiskTO;

import java.util.List;

public interface DiskService {
    List<DiskTO> getAll();

    void add(String disk);

    void delete(String disk);

    void refresh();
}
