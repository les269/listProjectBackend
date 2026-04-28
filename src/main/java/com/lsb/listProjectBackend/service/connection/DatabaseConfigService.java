package com.lsb.listProjectBackend.service.connection;

import com.lsb.listProjectBackend.domain.connection.ConnectionTestReqTO;
import com.lsb.listProjectBackend.domain.connection.ConnectionTestResultTO;
import com.lsb.listProjectBackend.domain.connection.DatabaseConfigTO;

import java.util.List;

public interface DatabaseConfigService {
    List<DatabaseConfigTO> getAll();

    DatabaseConfigTO getById(String configId);

    ConnectionTestResultTO testConnection(ConnectionTestReqTO to);

    void save(DatabaseConfigTO to);

    void delete(String configId);
}
