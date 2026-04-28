package com.lsb.listProjectBackend.controller.connection;

import com.lsb.listProjectBackend.domain.connection.ConnectionTestReqTO;
import com.lsb.listProjectBackend.domain.connection.ConnectionTestResultTO;
import com.lsb.listProjectBackend.domain.connection.DatabaseConfigTO;
import com.lsb.listProjectBackend.service.connection.DatabaseConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
public class DatabaseConfigController {

    @Autowired
    private DatabaseConfigService databaseConfigService;

    @GetMapping("/database-config/all")
    public List<DatabaseConfigTO> getAll() {
        return databaseConfigService.getAll();
    }

    @PostMapping("/database-config/test-connection")
    public ConnectionTestResultTO testConnection(@RequestBody ConnectionTestReqTO to) {
        return databaseConfigService.testConnection(to);
    }

    @PostMapping("/database-config/save")
    public void save(@RequestBody DatabaseConfigTO to) {
        databaseConfigService.save(to);
    }

    @DeleteMapping("/database-config/delete")
    public void delete(@RequestParam("configId") String configId) {
        databaseConfigService.delete(configId);
    }
}
