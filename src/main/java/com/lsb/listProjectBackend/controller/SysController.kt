package com.lsb.listProjectBackend.controller

import com.lsb.listProjectBackend.config.DynamicDataSourceRefresher
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin("*")
@RestController
@RequestMapping("api")
class SysController(
    private val jdbcTemplate: JdbcTemplate,
    private val dynamicDataSourceRefresher: DynamicDataSourceRefresher
) {
    /**
     * 檢查 local DB（local.sqlite）是否存活。
     * 使用預設 JdbcTemplate，綁定 @Primary 的 local DataSource。
     */
    @GetMapping("/sys/is-alive")
    fun isAlive(): ResponseEntity<String> {
        return try {
            // 嘗試執行簡單的 SQL 查詢來檢查連線是否正常
            jdbcTemplate.queryForObject("SELECT 1", Int::class.java)
            ResponseEntity.ok("Database is alive")
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Database is not alive: ${e.message}")
        }
    }
    /**
     * 依 local DB 的 {@code database_config} 重新註冊 dynamic 資料庫 targets。
     * {@code database_config} 變動後可呼叫此 API 進行 refresh。
     */
    @PostMapping("/sys/refresh-dynamic-datasource")
    fun refreshDynamicDatasource(): ResponseEntity<String> {
        return try {
            dynamicDataSourceRefresher.refresh()
            ResponseEntity.ok("Dynamic datasource refreshed successfully")
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to refresh dynamic datasource: ${e.message}")
        }
    }
}