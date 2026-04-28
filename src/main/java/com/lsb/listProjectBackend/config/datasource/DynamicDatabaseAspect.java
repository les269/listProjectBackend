package com.lsb.listProjectBackend.config.datasource;

import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.lsb.listProjectBackend.entity.local.Setting;
import com.lsb.listProjectBackend.repository.local.common.SettingRepository;
import com.lsb.listProjectBackend.utils.Global;

/**
 * AOP 切面：攔截標有 {@code @UseDynamic} 或 {@code @UseDynamicTx} 的方法，
 * 在方法執行前自動切換 dynamic 資料庫，方法結束後自動清理。
 *
 * 使用方式：
 * 在 Service 方法上加 @UseDynamic，不需要手動操作任何 DataSource。
 *
 * 執行流程：
 * 1. resolveCurrentDatabaseKey() 從 local.sqlite 的 setting 表
 * 讀取 name="current_dynamic_database" 的 value（即 config_name）
 * 2. 把 key 寫入 DynamicDataSourceContextHolder（ThreadLocal）
 * 3. depth + 1（處理巢狀呼叫）
 * 4. 執行原始方法（JPA 操作自動路由到正確 DB）
 * 5. finally：depth - 1；若降至 0（最外層）才清除 ThreadLocal
 *
 * 為什麼從 DB 讀 key，而非 config 檔寫死：
 * 此應用支援使用者在執行期間從 UI 切換要操作的資料庫，因此每次請求都需要
 * 動態讀取「目前使用者選了哪個 DB」的設定值。
 */
@Aspect
@Component
public class DynamicDatabaseAspect {

    private static final Logger log = LoggerFactory.getLogger(DynamicDatabaseAspect.class);

    private final SettingRepository settingRepository;

    public DynamicDatabaseAspect(SettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    /**
     * 攔截所有標有 {@code @UseDynamic}（類別或方法層級）或 {@code @UseDynamicTx} 的方法。
     *
     * @within 表示攔截整個類別（只要類別上有 @UseDynamic，其所有 public 方法都會被攔截）
     * @annotation 表示攔截單一方法
     *
     * @param pjp 被攔截方法的執行點，呼叫 {@code pjp.proceed()} 才會真正執行原始方法
     * @return 原始方法的回傳值
     */
    @Around("@within(com.lsb.listProjectBackend.aop.UseDynamic) || @annotation(com.lsb.listProjectBackend.aop.UseDynamic) || @annotation(com.lsb.listProjectBackend.aop.UseDynamicTx)")
    public Object switchDatabase(ProceedingJoinPoint pjp) throws Throwable {
        String dbKey = resolveCurrentDatabaseKey();
        DynamicDataSourceContextHolder.setDataSourceKey(dbKey);
        DynamicDataSourceContextHolder.incrementDepth();
        log.debug("Switched dynamic datasource to key: {}", dbKey);
        try {
            // 執行原始方法；此期間 JPA 操作都會透過 DynamicRoutingDataSource 路由到 dbKey 對應的 DB
            return pjp.proceed();
        } finally {
            // 無論正常結束或拋出例外，都確保清理 ThreadLocal，避免 thread pool 重用時殘留舊 key
            DynamicDataSourceContextHolder.decrementDepthAndClearIfOutermost();
            log.debug("Cleared dynamic datasource key (if outermost)");
        }
    }

    /**
     * 從 local.sqlite 的 {@code setting} 表讀取目前選擇的 dynamic DB key。
     *
     * - 若記錄存在且值不為空 → 回傳該值（對應某筆 database_config.config_name）
     * - 若記錄不存在 → 自動建立一筆預設記錄，並回傳預設 key
     * - 若讀取失敗（例如 DB 異常）→ 捕捉例外並 fallback 到預設 key，避免整個請求中斷
     *
     * @return 要使用的 DB routing key；失敗時回傳 {@link Global#DEFAULT_DYNAMIC_DB_KEY}
     */
    private String resolveCurrentDatabaseKey() {
        try {
            Optional<Setting> settingOpt = settingRepository.findById(Global.CURRENT_DYNAMIC_DB_SETTING_NAME);
            if (settingOpt.isPresent()) {
                String value = settingOpt.get().getValue();
                if (value != null && !value.isBlank()) {
                    return value;
                }
            } else {
                // 首次使用：自動建立預設設定，方便後續 UI 顯示與修改
                Setting setting = new Setting();
                setting.setName(Global.CURRENT_DYNAMIC_DB_SETTING_NAME);
                setting.setValue(Global.DEFAULT_DYNAMIC_DB_KEY);
                setting.setEnabled(true);
                settingRepository.save(setting);
            }
        } catch (Exception e) {
            log.warn("Failed to resolve current dynamic database from setting, fallback to default", e);
        }
        return Global.DEFAULT_DYNAMIC_DB_KEY;
    }
}
