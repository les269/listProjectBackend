package com.lsb.listProjectBackend.service;

import com.lsb.listProjectBackend.domain.ConnectionTestReqTO;
import com.lsb.listProjectBackend.domain.ConnectionTestResultTO;
import com.lsb.listProjectBackend.domain.DatabaseConfigTO;

import java.util.List;

public interface DatabaseConfigService {
    List<DatabaseConfigTO> getAll();

    DatabaseConfigTO getById(String configId);

    ConnectionTestResultTO testConnection(ConnectionTestReqTO to);

    void save(DatabaseConfigTO to);

    void delete(String configId);
}
