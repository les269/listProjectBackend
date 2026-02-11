package com.lsb.listProjectBackend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lsb.listProjectBackend.config.DynamicDataSourceRefresher;
import com.lsb.listProjectBackend.entity.local.Setting;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
public class SysController {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private DynamicDataSourceRefresher dynamicDataSourceRefresher;

    /**
     * 檢查 local DB（local.sqlite）是否存活。
     * 使用預設 JdbcTemplate，綁定 @Primary 的 local DataSource。
     */
    @GetMapping("/sys/is-alive")
    public ResponseEntity<String> isAlive() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return new ResponseEntity<>("Database is alive", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Database is down: " + e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    /**
     * 依 local DB 的 {@code database_config} 重新註冊 dynamic 資料庫 targets。
     * {@code database_config} 變動後可呼叫此 API 進行 refresh。
     */
    @PostMapping("/sys/refresh-dynamic-datasource")
    public ResponseEntity<String> refreshDynamicDatasource() {
        try {
            dynamicDataSourceRefresher.refresh();
            return new ResponseEntity<>("Dynamic datasource refreshed", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Refresh failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
