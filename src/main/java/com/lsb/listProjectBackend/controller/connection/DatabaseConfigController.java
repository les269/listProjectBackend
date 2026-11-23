package com.lsb.listProjectBackend.controller.connection;

import com.lsb.listProjectBackend.domain.connection.ConnectionTestReqTO;
import com.lsb.listProjectBackend.domain.connection.ConnectionTestResultTO;
import com.lsb.listProjectBackend.domain.connection.DatabaseConfigTO;
import com.lsb.listProjectBackend.service.connection.DatabaseConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/database-config")
@RequiredArgsConstructor
public class DatabaseConfigController {
    private final DatabaseConfigService databaseConfigService;

    @GetMapping("/all")
    public List<DatabaseConfigTO> getAll() {
        return databaseConfigService.getAll();
    }

    @PostMapping("/test-connection")
    public ConnectionTestResultTO testConnection(@RequestBody ConnectionTestReqTO to) {
        return databaseConfigService.testConnection(to);
    }

    @PostMapping("/save")
    public void save(@RequestBody DatabaseConfigTO to) {
        databaseConfigService.save(to);
    }

    @DeleteMapping("/delete")
    public void delete(@RequestParam("configId") String configId) {
        databaseConfigService.delete(configId);
    }

    @GetMapping("/open-local-folder")
    public boolean openLocalFolder(@RequestParam("configId") String configId) throws Exception {
        return databaseConfigService.openLocalFolder(configId);
    }
}
