package com.lsb.listProjectBackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
public class SysController {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/sys/is-alive")
    public ResponseEntity<String> isAlive() {
        try {
            // 使用簡單查詢來檢查數據庫的狀態
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return new ResponseEntity<>("Database is alive", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Database is down: " + e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
